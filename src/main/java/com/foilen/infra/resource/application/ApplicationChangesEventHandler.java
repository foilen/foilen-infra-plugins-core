/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerResourceStream;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.dns.DnsPointer;
import com.foilen.infra.resource.domain.DomainResourceHelper;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.SystemUnixUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.StreamTools;
import com.google.common.base.Joiner;

public class ApplicationChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        List<ActionHandler> actions = new ArrayList<>();

        logger.info("Search for modified RUN_AS");

        // Updated Unix Users
        Set<String> applicationNamesToCheck = new ChangesEventHandlerResourceStream<>(UnixUser.class) //
                .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources()) //
                .streamFromResourceClassAndLinkType(services, Application.class, LinkTypeConstants.RUN_AS) //

                // Changes in the RUN_AS links
                .linksAddFrom(changesInTransactionContext.getLastAddedLinks(), LinkTypeConstants.RUN_AS, UnixUser.class) //
                .linksAddFrom(changesInTransactionContext.getLastDeletedLinks(), LinkTypeConstants.RUN_AS, UnixUser.class) //

                // Changes in the application runAs
                .resourcesAdd(new ChangesEventHandlerResourceStream<>(Application.class) //
                        .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources()) //
                ) //

                .getResourcesStream() //
                .map(it -> it.getName()) //
                .collect(Collectors.toSet());

        applicationNamesToCheck.forEach(applicationName -> {

            actions.add((s, changes) -> {
                logger.info("Update \"runAs\" with the link RUN_AS -> UnixUser");

                IPResourceService resourceService = s.getResourceService();

                Optional<Application> o = resourceService.resourceFindByPk(new Application(applicationName));
                if (!o.isPresent()) {
                    logger.info("{} is not present. Skipping", applicationName);
                    return;
                }
                Application application = o.get();

                // Update the running user
                List<UnixUser> unixUsers = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(application, LinkTypeConstants.RUN_AS, UnixUser.class);
                if (unixUsers.size() > 1) {
                    throw new IllegalUpdateException("An application cannot have multiple users to run as (only 0 or 1)");
                }
                Long neededRunAs = null;
                if (!unixUsers.isEmpty()) {
                    neededRunAs = unixUsers.get(0).getId();
                }
                Long currentRunAs = application.getApplicationDefinition().getRunAs();
                logger.debug("neededRunAs: {} ; currentRunAs: {}", neededRunAs, currentRunAs);
                if ((neededRunAs == null && currentRunAs != null) || (neededRunAs != null && !neededRunAs.equals(currentRunAs))) {
                    logger.debug("Updating runAs to: {}", neededRunAs);
                    application.getApplicationDefinition().setRunAs(neededRunAs);
                    changes.resourceUpdate(application);
                }

            });

        });
        applicationNamesToCheck.clear();

        // Create and manage one DnsPointer per "domainNames" ; POINTS_TO Machines that this application is installed on
        // Also manage Domains
        applicationNamesToCheck
                .addAll(ChangesEventHandlerUtils.getFromResourcesStream(changesInTransactionContext.getLastAddedLinks(), Application.class, LinkTypeConstants.INSTALLED_ON, Machine.class)//
                        .map(Application::getName).collect(Collectors.toList()));
        applicationNamesToCheck.addAll(ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), Application.class) //
                .map(Application::getName).collect(Collectors.toList()));
        applicationNamesToCheck.addAll(ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastRefreshedResources(), Application.class) //
                .map(Application::getName).collect(Collectors.toList()));
        applicationNamesToCheck.addAll(ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), Application.class) //
                .filter(updatedResource -> !((Application) updatedResource.getPrevious()).getDomainNames().equals(((Application) updatedResource.getNext()).getDomainNames())) //
                .map(updatedResource -> ((Application) updatedResource.getNext()).getName()) //
                .collect(Collectors.toList()));

        applicationNamesToCheck.forEach(applicationName -> {

            actions.add((s, changes) -> {

                IPResourceService resourceService = s.getResourceService();

                Application application = resourceService.resourceFindByPk(new Application(applicationName)).get();

                logger.info("Update DnsPointers for Application {}", applicationName);

                SortedSet<String> desiredDomainNames = application.getDomainNames();
                logger.debug("Has domains {}", desiredDomainNames);

                List<Machine> installOnMachines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(application, LinkTypeConstants.INSTALLED_ON, Machine.class);
                logger.debug("Is installed on {}", installOnMachines);
                List<DnsPointer> dnsPointers = new ArrayList<>(resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(application, LinkTypeConstants.MANAGES, DnsPointer.class));
                logger.debug("Has pointers {}", dnsPointers);

                // Delete no more needed
                Set<String> domainsInDnsPointer = new HashSet<>();
                Iterator<DnsPointer> dnsPointersIt = dnsPointers.iterator();
                while (dnsPointersIt.hasNext()) {
                    DnsPointer dnsPointer = dnsPointersIt.next();
                    String domainName = dnsPointer.getName();
                    if (desiredDomainNames.contains(domainName)) {
                        domainsInDnsPointer.add(domainName);
                    } else {
                        dnsPointersIt.remove();
                        changes.resourceDelete(dnsPointer);
                        logger.debug("Removing {}", dnsPointer);
                    }
                }

                // Add missing
                desiredDomainNames.forEach(domainName -> {
                    if (!domainsInDnsPointer.contains(domainName)) {
                        domainsInDnsPointer.add(domainName);
                        DnsPointer dnsPointer = new DnsPointer(domainName);
                        logger.debug("Adding {}", dnsPointer);
                        dnsPointers.add(dnsPointer);
                        changes.resourceAdd(dnsPointer);
                        changes.linkAdd(application, LinkTypeConstants.MANAGES, dnsPointer);
                    }
                });

                // Sync links
                dnsPointers.forEach(dnsPointer -> {
                    logger.info("Update DnsPointer {} links to machines", dnsPointer);
                    List<Machine> previousToResources = Collections.emptyList();
                    if (dnsPointer.getInternalId() != null) {
                        previousToResources = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(dnsPointer, LinkTypeConstants.POINTS_TO, Machine.class);
                    }
                    logger.info("Has links to machines {}", previousToResources);
                    CommonResourceLink.syncToLinks(services, changes, dnsPointer, LinkTypeConstants.POINTS_TO, Machine.class, installOnMachines);

                });

                // Sync managed domain names
                DomainResourceHelper.syncManagedLinks(s, changes, application, application.getDomainNames());

            });

        });

        // When portsExposed changed or the installed on machines changed, check that all Applications on each Machine that it is installed on has unique ports exposed per machine
        StreamTools.concat( //
                ChangesEventHandlerUtils.getToResourcesStream(changesInTransactionContext.getLastAddedLinks(), Application.class, LinkTypeConstants.INSTALLED_ON, Machine.class), //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), Application.class).flatMap(application -> {
                    return services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(application, LinkTypeConstants.INSTALLED_ON, Machine.class).stream();
                }), //
                ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), Application.class).filter(updatedResource -> {
                    IPApplicationDefinition previousAppDef = ((Application) updatedResource.getPrevious()).getApplicationDefinition();
                    IPApplicationDefinition nextAppDef = ((Application) updatedResource.getNext()).getApplicationDefinition();
                    return !previousAppDef.getPortsExposed().equals(nextAppDef.getPortsExposed()) || !previousAppDef.getUdpPortsExposed().equals(nextAppDef.getUdpPortsExposed());
                }) //
                        .flatMap(application -> {
                            return services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(application.getNext(), LinkTypeConstants.INSTALLED_ON, Machine.class).stream();
                        }) //
        ) //
                .sorted().distinct() //
                .forEach(machine -> {

                    actions.add((s, changes) -> {

                        logger.info("Validate exposed ports for all applications on machine {}", machine.getName());

                        IPResourceService resourceService = s.getResourceService();

                        List<Application> applicationsOnMachine = resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(Application.class, LinkTypeConstants.INSTALLED_ON, machine);
                        Set<Integer> endpointPorts = new HashSet<>();
                        for (Application application : applicationsOnMachine) {
                            for (Integer port : application.getApplicationDefinition().getPortsExposed().keySet()) {
                                if (!endpointPorts.add(port)) {
                                    throw new IllegalUpdateException("The port " + port + " is exposed by many applications installed on " + machine.getName());
                                }
                            }
                        }

                    });

                });

        // Check mounted volumes host path belong to the unix users
        ChangesEventHandlerResourceStream<Application> allChangedApps = new ChangesEventHandlerResourceStream<>(Application.class);
        allChangedApps.resourcesAddOfType(changesInTransactionContext.getLastAddedResources());
        allChangedApps.resourcesAddOfType(changesInTransactionContext.getLastRefreshedResources());
        allChangedApps.resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        allChangedApps.linksAddFromAndTo(changesInTransactionContext.getLastAddedLinks());
        allChangedApps.linksAddFromAndTo(changesInTransactionContext.getLastDeletedLinks());

        allChangedApps.getResourcesStream() //
                .map(Application::getName) //
                .sorted().distinct() //
                .forEach(applicationName -> {

                    actions.add((s, changes) -> {

                        IPResourceService resourceService = s.getResourceService();

                        logger.info("Check mounted volumes for Application {} are allowed", applicationName);
                        Optional<Application> applicationOptional = resourceService.resourceFindByPk(new Application(applicationName));
                        if (applicationOptional.isEmpty()) {
                            logger.info("Application {} doesn't exist. Skipping");
                            return;
                        }

                        Application application = applicationOptional.get();

                        List<String> hostFolders = application.getApplicationDefinition().getVolumes().stream() //
                                .filter(it -> it.getHostFolder() != null) //
                                .map(it -> it.getHostFolder()) //
                                .sorted().distinct() //
                                .collect(Collectors.toList());

                        if (!hostFolders.isEmpty()) {
                            // Get the unix user
                            logger.info("There are hosts folders. Getting the unix user that is running it");
                            List<UnixUser> unixUsers = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(application, LinkTypeConstants.RUN_AS, UnixUser.class);
                            if (unixUsers.isEmpty()) {
                                logger.info("There are no unix user running it. Skipping");
                                return;
                            }

                            UnixUser unixUser = unixUsers.get(0);
                            String homeFolder = unixUser.getHomeFolder();
                            if (homeFolder != null) {
                                homeFolder = DirectoryTools.pathTrailingSlash(homeFolder);
                            }
                            logger.info("Unix User - name: {}, id: {}, home: {}", unixUser.getName(), unixUser.getId(), homeFolder);

                            // Check if System user
                            if (unixUser instanceof SystemUnixUser) {
                                logger.info("The unix user is of type System. Skipping");
                                return;
                            }

                            // Check all hostFolders are valid
                            for (String hostFolder : hostFolders) {
                                hostFolder = DirectoryTools.pathTrailingSlash(hostFolder);
                                boolean ok = hostFolder.startsWith(homeFolder);
                                logger.info("Host Folder {} : OK? {}", hostFolder, ok);
                                if (!ok) {
                                    throw new IllegalUpdateException("All mounted host volumes must be under the UnixUser");
                                }
                            }

                        }

                    });

                });

        // Check working directories and commands doesn't have non-path characters
        List<String> wrongs = new ArrayList<>();
        allChangedApps.getResourcesStream().forEach(app -> {
            if (!CommonValidation.validPath(app.getApplicationDefinition().getCommand())) {
                wrongs.add("Application " + app.getName() + " Main Command");
            }
            if (!CommonValidation.validPath(app.getApplicationDefinition().getWorkingDirectory())) {
                wrongs.add("Application " + app.getName() + " Main Working Directory");
            }
            app.getApplicationDefinition().getServices().forEach(appService -> {

                if (!CommonValidation.validPath(appService.getCommand())) {
                    wrongs.add("Application " + app.getName() + " Service " + appService.getName() + " Command");
                }
                if (!CommonValidation.validPath(appService.getWorkingDirectory())) {
                    wrongs.add("Application " + app.getName() + " Service " + appService.getName() + " Working Directory");
                }

            });
        });
        if (!wrongs.isEmpty()) {
            throw new IllegalUpdateException("All commands and working directories cannot have non-path characters: " + Joiner.on(",").join(wrongs));
        }

        return actions;
    }

}
