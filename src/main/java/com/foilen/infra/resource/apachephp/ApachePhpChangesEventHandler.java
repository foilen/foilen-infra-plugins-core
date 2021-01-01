/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerResourceStream;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.AttachablePartContext;
import com.foilen.infra.resource.composableapplication.ComposableApplication;
import com.foilen.infra.resource.composableapplication.util.AttachablePartUpdatedUtils;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.utils.ActionsHandlerUtils;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.FreemarkerTools;
import com.foilen.smalltools.tools.JsonTools;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class ApachePhpChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    private void addOrUpdate(IPResource resource, ChangesContext changes) {
        if (resource.getInternalId() == null) {
            changes.resourceAdd(resource);
        } else {
            changes.resourceUpdate(resource);
        }
    }

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        List<ActionHandler> actions = new ArrayList<>();

        ChangesEventHandlerResourceStream<ApachePhp> apachePhpStream = new ChangesEventHandlerResourceStream<>(ApachePhp.class);
        apachePhpStream.resourcesAddOfType(changesInTransactionContext.getLastAddedResources());
        apachePhpStream.resourcesAddOfType(changesInTransactionContext.getLastRefreshedResources());
        apachePhpStream.resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        apachePhpStream.linksAddFromAndTo(changesInTransactionContext.getLastAddedLinks());
        apachePhpStream.linksAddFromAndTo(changesInTransactionContext.getLastDeletedLinks());
        apachePhpStream.resourcesAdd(AttachablePartUpdatedUtils.lastChanges(services, changesInTransactionContext, ApachePhp.class));

        apachePhpStream.resourcesAdd(new ChangesEventHandlerResourceStream<>(UnixUser.class) //
                .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources()) //
                .streamFromResourceClassAndLinkType(services, ApachePhp.class, LinkTypeConstants.RUN_AS));
        apachePhpStream.resourcesAdd(new ChangesEventHandlerResourceStream<>(Machine.class) //
                .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources()) //
                .streamFromResourceClassAndLinkType(services, ApachePhp.class, LinkTypeConstants.INSTALLED_ON));
        apachePhpStream.resourcesAdd(new ChangesEventHandlerResourceStream<>(ApachePhpHtPasswd.class) //
                .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources()) //
                .streamFromResourceClassAndLinkType(services, ApachePhp.class, LinkTypeConstants.USES));
        apachePhpStream.resourcesAdd(new ChangesEventHandlerResourceStream<>(ApachePhpFolder.class) //
                .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources()) //
                .streamFromResourceClassAndLinkType(services, ApachePhp.class, LinkTypeConstants.USES));

        apachePhpStream.getResourcesStream() //
                .map(it -> it.getName()) //
                .sorted().distinct() //
                .forEach(apachePhpName -> {

                    actions.add((s, changes) -> {

                        logger.info("Processing apache php {}", apachePhpName);

                        IPResourceService resourceService = services.getResourceService();
                        Optional<ApachePhp> o = resourceService.resourceFindByPk(new ApachePhp(apachePhpName));
                        if (!o.isPresent()) {
                            logger.info("{} is not present. Skipping", apachePhpName);
                            return;
                        }
                        ApachePhp apachePhp = o.get();

                        // Get the user and machines
                        List<UnixUser> unixUsers = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(apachePhp, LinkTypeConstants.RUN_AS, UnixUser.class);
                        List<Machine> machines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(apachePhp, LinkTypeConstants.INSTALLED_ON, Machine.class);
                        List<ApachePhpFolder> folders = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(apachePhp, LinkTypeConstants.USES, ApachePhpFolder.class);
                        List<ApachePhpHtPasswd> htPasswds = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(apachePhp, LinkTypeConstants.USES, ApachePhpHtPasswd.class);
                        List<AttachablePart> attachedParts = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(apachePhp, ComposableApplication.LINK_TYPE_ATTACHED,
                                AttachablePart.class);
                        List<Website> websitesFrom = resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(Website.class, LinkTypeConstants.POINTS_TO, apachePhp);

                        List<Application> desiredManagedApplications = new ArrayList<>();

                        logger.debug("[{}] Running as {} on {}", apachePhpName, unixUsers, machines);

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
                        if (unixUsers.isEmpty()) {
                            logger.info("No unix user to run as. Skipping");
                            proceed = false;
                        }

                        if (proceed) {

                            UnixUser unixUser = unixUsers.get(0);
                            Long unixUserId = unixUser.getId();

                            // Application
                            Application application = ActionsHandlerUtils.getOrCreateAnApplication(resourceService, apachePhp.getName());
                            application.setDescription(apachePhp.getResourceDescription());

                            IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
                            application.setApplicationDefinition(applicationDefinition);
                            applicationDefinition.setRunAs(unixUserId);

                            // Sort by aliases ; deeper first
                            Collections.sort(folders, (a, b) -> {
                                int result = Integer.compare(b.getAlias().length(), a.getAlias().length()); // Longer first
                                if (result == 0) {
                                    result = a.getAlias().compareTo(b.getAlias());
                                }
                                return result;
                            });

                            applicationDefinition.setFrom("foilen/fcloud-docker-apache_php:" + apachePhp.getVersion());

                            // Apache and PHP config
                            IPApplicationDefinitionAssetsBundle assetsBundle = applicationDefinition.addAssetsBundle();
                            assetsBundle.addAssetResource("/etc/apache2/ports.conf", "/com/foilen/infra/resource/apachephp/apache-ports.conf");
                            assetsBundle.addAssetResource("/apache-start.sh", "/com/foilen/infra/resource/apachephp/apache-start.sh");
                            assetsBundle.addAssetResource("/copy-php-conf.sh", "/com/foilen/infra/resource/apachephp/copy-php-conf.sh");

                            int maxUploadFilesizeM = apachePhp.getMaxUploadFilesizeM();
                            int memoryLimit = apachePhp.getMaxMemoryM();

                            Map<String, Object> iniConfigModel = new HashMap<>();
                            iniConfigModel.put("defaultEmailFrom", apachePhp.getDefaultEmailFrom());
                            iniConfigModel.put("upload_max_filesize", maxUploadFilesizeM);
                            iniConfigModel.put("memory_limit", memoryLimit);
                            iniConfigModel.put("max_file_uploads", 100);

                            assetsBundle.addAssetContent("/99-fcloud.ini", FreemarkerTools.processTemplate("/com/foilen/infra/resource/apachephp/php.ini.ftl", iniConfigModel));
                            if (!Strings.isNullOrEmpty(apachePhp.getDefaultEmailFrom())) {
                                Map<String, String> config = Collections.singletonMap("defaultFrom", apachePhp.getDefaultEmailFrom());
                                assetsBundle.addAssetContent("/etc/sendmail-to-msmtp.json", JsonTools.prettyPrint(config));
                            }

                            // Site configuration
                            Map<String, Object> model = new HashMap<>();
                            applicationDefinition.addVolume(new IPApplicationDefinitionVolume(apachePhp.getBasePath(), "/base"));
                            model.put("logMaxSizeM", apachePhp.getLogMaxSizeM());
                            model.put("baseFolder", "/base");
                            model.put("mainSiteRelativePath", sanitisePath(apachePhp.getMainSiteRelativePath(), true, true));

                            List<Map<String, String>> containerFolders = folders.stream() //
                                    .map(it -> {
                                        Map<String, String> alias = new HashMap<>();
                                        alias.put("alias", sanitisePath(it.getAlias(), true, false));
                                        alias.put("folder", "/folders/" + sanitisePath(it.getBasePath(), false, false).replaceAll("\\/", "_") + "/" + sanitisePath(it.getRelativePath(), false, false));
                                        return alias;
                                    }) //
                                    .collect(Collectors.toList());
                            model.put("aliases", containerFolders);
                            model.put("useBasicAuth", !htPasswds.isEmpty());
                            for (int i = 0; i < folders.size(); ++i) {
                                ApachePhpFolder folder = folders.get(i);
                                applicationDefinition.addVolume(new IPApplicationDefinitionVolume( //
                                        sanitisePath(folder.getBasePath(), true, true), //
                                        "/folders/" + sanitisePath(folder.getBasePath(), false, false).replaceAll("\\/", "_")));
                            }

                            // /htpasswd file
                            if (!htPasswds.isEmpty()) {
                                List<String> htpasswdContent = htPasswds.stream() //
                                        .filter(it -> !Strings.isNullOrEmpty(it.getUser()) && !Strings.isNullOrEmpty(it.getPassword())) //
                                        .sorted((a, b) -> a.getUser().compareTo(b.getUser())) //
                                        .map(it -> it.getUser() + ":{SHA}" + Base64.getEncoder().encodeToString(sha1(it.getPassword()))) //
                                        .collect(Collectors.toList());
                                assetsBundle.addAssetContent("/htpasswd", Joiner.on('\n').join(htpasswdContent));
                                applicationDefinition.addBuildStepCommand("chown www-data:www-data /htpasswd && chmod 600 /htpasswd");
                            }

                            assetsBundle.addAssetContent("/etc/apache2/sites-enabled/000-default.conf",
                                    FreemarkerTools.processTemplate("/com/foilen/infra/resource/apachephp/apache-http-fs.ftl", model));

                            applicationDefinition.addBuildStepCommand("chmod 644 /etc/apache2/ports.conf /99-fcloud.ini && chmod +x /*.sh && sync && /copy-php-conf.sh");

                            applicationDefinition.addVolume(new IPApplicationDefinitionVolume(null, "/var/lock/apache2", unixUserId, unixUserId, "755"));

                            applicationDefinition.addContainerUserToChangeId("www-data", unixUserId);

                            applicationDefinition.addBuildStepCommand("chmod -R 777 /var/log && chown www-data:www-data /var/run/apache2");
                            applicationDefinition.addService("apache", "/apache-start.sh");

                            // Log folder
                            if (unixUser.getHomeFolder() != null) {
                                String baseFolder = unixUser.getHomeFolder() + "/apache_php/" + apachePhp.getName();
                                applicationDefinition.addVolume(new IPApplicationDefinitionVolume(baseFolder + "/log/apache2", "/var/log/apache2", unixUser.getId(), unixUser.getId(), "770"));
                            }

                            // Enable modules
                            applicationDefinition.addBuildStepCommand("/usr/sbin/a2enmod rewrite");

                            applicationDefinition.addPortEndpoint(8080, DockerContainerEndpoints.HTTP_TCP);

                            // Attach parts in a deterministic order
                            logger.debug("attachedParts ; amount {}", attachedParts.size());
                            attachedParts.stream() //
                                    .sorted((a, b) -> a.getResourceName().compareTo(b.getResourceName())) //
                                    .forEach(attachedPart -> {
                                        logger.debug("Attaching {} with type {}", attachedPart.getResourceName(), attachedPart.getClass().getName());
                                        attachedPart.attachTo(new AttachablePartContext().setServices(services).setApplication(application).setApplicationDefinition(applicationDefinition));
                                    });

                            addOrUpdate(application, changes);
                            desiredManagedApplications.add(application);

                            // Sync links
                            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.INSTALLED_ON, Machine.class, machines);
                            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.RUN_AS, UnixUser.class, unixUsers);

                            // website -> LinkTypeConstants.POINTS_TO -> application
                            CommonResourceLink.syncFromLinks(services, changes, Website.class, LinkTypeConstants.POINTS_TO, application, websitesFrom);

                        }

                        CommonResourceLink.syncToLinks(services, changes, apachePhp, LinkTypeConstants.MANAGES, Application.class, desiredManagedApplications);

                    });

                });

        return actions;
    }

    private String sanitisePath(String path, boolean startsWithSlash, boolean endsWithSlash) {
        if (startsWithSlash) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        } else {
            while (path.startsWith("/")) {
                path = path.length() > 1 ? path.substring(1) : "";
            }
        }
        if (endsWithSlash) {
            if (!path.endsWith("/")) {
                path = path + "/";
            }
        } else {
            while (path.endsWith("/")) {
                path = path.length() > 1 ? path.substring(0, path.length() - 1) : "";
            }
        }
        return path.replaceAll("\\/\\/", "/");
    }

    private byte[] sha1(String in) {
        try {
            return MessageDigest.getInstance("SHA1").digest(in.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
