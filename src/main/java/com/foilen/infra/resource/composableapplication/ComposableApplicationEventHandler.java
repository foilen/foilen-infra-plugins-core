/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication;

import java.util.ArrayList;
import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractCommonMethodUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.CommonMethodUpdateEventHandlerContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.tools.CollectionsTools;
import com.foilen.smalltools.tools.StringTools;

public class ComposableApplicationEventHandler extends AbstractCommonMethodUpdateEventHandler<ComposableApplication> {

    @Override
    public void checkFarLinkChanged(CommonServicesContext services, ChangesContext changes, ComposableApplication resource) {
        // NO-OP
    }

    @Override
    protected void commonHandlerExecute(CommonServicesContext services, ChangesContext changes, CommonMethodUpdateEventHandlerContext<ComposableApplication> context) {

        context.setManagedResourcesUpdateContentIfExists(true);

        context.addManagedResourceTypes(Application.class);

        IPResourceService resourceService = services.getResourceService();

        ComposableApplication composableApplication = context.getResource();

        // Get the links
        List<Machine> machines = new ArrayList<>();
        List<UnixUser> unixUsers = new ArrayList<>();
        List<AttachablePart> attachableParts = new ArrayList<>();

        resourceService.linkFindAllByFromResource(composableApplication).stream() //
                .sorted((a, b) -> StringTools.safeComparisonNullFirst(a.getB().getResourceName(), b.getB().getResourceName())) //
                .forEach(link -> {
                    switch (link.getA()) {
                    case LinkTypeConstants.INSTALLED_ON:
                        if (link.getB() instanceof Machine) {
                            machines.add((Machine) link.getB());
                        }
                        break;
                    case LinkTypeConstants.RUN_AS:
                        if (link.getB() instanceof UnixUser) {
                            unixUsers.add((UnixUser) link.getB());
                        }
                        break;
                    case ComposableApplication.LINK_TYPE_ATTACHED:
                        if (link.getB() instanceof AttachablePart) {
                            attachableParts.add((AttachablePart) link.getB());
                        }
                        break;

                    default:
                        break;
                    }
                });

        // Validate links
        boolean proceed = true;
        if (machines.isEmpty()) {
            logger.info("No machine to install on. Skipping");
            proceed = false;
        }
        if (unixUsers.size() > 1) {
            logger.warn("Too many unix user to run as");
            throw new IllegalUpdateException("Must have a singe unix user to run as. Got " + unixUsers.size());
        }

        // Validate environments
        if (!CollectionsTools.isNullOrEmpty(composableApplication.getEnvironments())) {
            composableApplication.getEnvironments().forEach(it -> {
                String[] pair = it.split("=");
                if (pair.length != 2) {
                    logger.warn("Error environment format for {}", it);
                    throw new IllegalUpdateException(services.getTranslationService().translate("error.environmentFormat"));
                }
            });
        }

        // Validate portsExposedTcp
        if (!CollectionsTools.isNullOrEmpty(composableApplication.getPortsExposedTcp())) {
            composableApplication.getPortsExposedTcp().forEach(it -> {
                String[] pair = it.split(":");
                if (pair.length != 2) {
                    logger.warn("Error portsExposedTcp format for {}", it);
                    throw new IllegalUpdateException(services.getTranslationService().translate("error.portsExposedTcpFormat"));
                }
                try {
                    Integer.valueOf(pair[0]);
                    Integer.valueOf(pair[1]);
                } catch (Exception e) {
                    throw new IllegalUpdateException(services.getTranslationService().translate("error.portsExposedTcpFormat"));
                }
            });
        }

        // Validate portsExposedUdp
        if (!CollectionsTools.isNullOrEmpty(composableApplication.getPortsExposedUdp())) {
            composableApplication.getPortsExposedUdp().forEach(it -> {
                String[] pair = it.split(":");
                if (pair.length != 2) {
                    logger.warn("Error portsExposedUdp format for {}", it);
                    throw new IllegalUpdateException(services.getTranslationService().translate("error.portsExposedUdpFormat"));
                }
                try {
                    Integer.valueOf(pair[0]);
                    Integer.valueOf(pair[1]);
                } catch (Exception e) {
                    throw new IllegalUpdateException(services.getTranslationService().translate("error.portsExposedUdpFormat"));
                }
            });
        }

        // Validate portsEndpoint
        if (!CollectionsTools.isNullOrEmpty(composableApplication.getPortsEndpoint())) {
            composableApplication.getPortsEndpoint().forEach(it -> {
                String[] pair = it.split(":");
                if (pair.length != 2) {
                    logger.warn("Error portsEndpoint format for {}", it);
                    throw new IllegalUpdateException(services.getTranslationService().translate("error.portsEndpointFormat"));
                }
                try {
                    Integer.valueOf(pair[0]);
                } catch (Exception e) {
                    throw new IllegalUpdateException(services.getTranslationService().translate("error.portsEndpointFormat"));
                }
            });
        }

        Long unixUserId = null;
        UnixUser unixUser = null;
        if (!unixUsers.isEmpty()) {
            unixUser = unixUsers.get(0);
            unixUserId = unixUser.getId();
        }

        if (proceed) {

            logger.debug("attachableParts ; amount {}", attachableParts.size());

            // Create an Application
            Application application = new Application();
            context.addManagedResources(application);
            application.setName(composableApplication.getName());
            application.setDescription(composableApplication.getResourceDescription());

            IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
            application.setApplicationDefinition(applicationDefinition);
            applicationDefinition.setFrom(composableApplication.getFrom());
            applicationDefinition.setRunAs(unixUserId);
            applicationDefinition.setCommand(composableApplication.getMainCommand());
            applicationDefinition.setWorkingDirectory(composableApplication.getMainWorkingDirectory());

            // Add environments
            if (!CollectionsTools.isNullOrEmpty(composableApplication.getEnvironments())) {
                composableApplication.getEnvironments().forEach(it -> {
                    String[] pair = it.split("=");
                    logger.debug("Adding environment {} : {}", pair[0], pair[1]);
                    applicationDefinition.getEnvironments().put(pair[0], pair[1]);
                });
            }

            // Validate portsExposedTcp
            if (!CollectionsTools.isNullOrEmpty(composableApplication.getPortsExposedTcp())) {
                composableApplication.getPortsExposedTcp().forEach(it -> {
                    String[] pair = it.split(":");
                    logger.debug("Adding exposed TCP port {} : {}", pair[0], pair[1]);
                    applicationDefinition.addPortExposed(Integer.valueOf(pair[0]), Integer.valueOf(pair[1]));
                });
            }

            // Validate portsExposedUdp
            if (!CollectionsTools.isNullOrEmpty(composableApplication.getPortsExposedUdp())) {
                composableApplication.getPortsExposedUdp().forEach(it -> {
                    String[] pair = it.split(":");
                    logger.debug("Adding exposed UDP port {} : {}", pair[0], pair[1]);
                    applicationDefinition.addUdpPortExposed(Integer.valueOf(pair[0]), Integer.valueOf(pair[1]));
                });
            }

            // Validate portsEndpoint
            if (!CollectionsTools.isNullOrEmpty(composableApplication.getPortsEndpoint())) {
                composableApplication.getPortsEndpoint().forEach(it -> {
                    String[] pair = it.split(":");
                    logger.debug("Adding endpoint port {} : {}", pair[0], pair[1]);
                    applicationDefinition.getPortsEndpoint().put(Integer.valueOf(pair[0]), pair[1]);
                });
            }

            // Attach parts in a deterministic order
            attachableParts.stream() //
                    .sorted((a, b) -> a.getResourceName().compareTo(b.getResourceName())) //
                    .forEach(attachablePart -> {
                        logger.debug("Attaching {} with type {}", attachablePart.getResourceName(), attachablePart.getClass().getName());
                        AttachablePartContext attachablePartContext = new AttachablePartContext();
                        attachablePartContext.setServices(services);
                        attachablePartContext.setApplication(application);
                        attachablePartContext.setApplicationDefinition(applicationDefinition);
                        attachablePart.attachTo(attachablePartContext);
                    });

            // Link machines
            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.INSTALLED_ON, Machine.class, machines);

            // Link unix user
            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.RUN_AS, UnixUser.class, unixUsers);

            // Sync link websites
            List<Website> websitesFrom = resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(Website.class, LinkTypeConstants.POINTS_TO, composableApplication);
            CommonResourceLink.syncFromLinks(services, changes, Website.class, LinkTypeConstants.POINTS_TO, application, websitesFrom);

        }

    }

    @Override
    public Class<ComposableApplication> supportedClass() {
        return ComposableApplication.class;
    }

}
