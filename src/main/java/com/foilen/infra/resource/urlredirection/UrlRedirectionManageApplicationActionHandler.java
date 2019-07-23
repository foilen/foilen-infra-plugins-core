/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.urlredirection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.FreemarkerTools;
import com.google.common.base.Strings;

/**
 * For each Machine that has UrlRedirections to it:
 * <ul>
 * <li>Create 1 Application for http redirections</li>
 * <li>Create 1 Application for https redirections</li>
 * </ul>
 */
public class UrlRedirectionManageApplicationActionHandler extends AbstractBasics implements ActionHandler {

    private String machineName;

    public UrlRedirectionManageApplicationActionHandler(String machineName) {
        this.machineName = machineName;
    }

    private void createApacheApplication( //
            CommonServicesContext services, ChangesContext changes, //
            Machine machine, UnixUser unixUser, boolean isHttps, //
            List<UrlRedirection> urlRedirections) {

        String protocol = isHttps ? "HTTPS" : "HTTP";
        String machineName = machine.getName();
        Long unixUserId = unixUser.getId();

        Application application = new Application();
        String applicationName = "infra_url_redirection_" + protocol.toLowerCase() + "-" + machineName.replaceAll("\\.", "_");
        application.setName(applicationName);
        application.setDescription("Apache " + protocol + " URL redirections for " + machineName);

        if (urlRedirections.isEmpty()) {
            logger.info("There is no url redirection for machine {}. Removing the link to the application if any", machineName);
            changes.linkDelete(machine, LinkTypeConstants.MANAGES, application);
            return;
        }

        IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
        application.setApplicationDefinition(applicationDefinition);
        applicationDefinition.setRunAs(unixUserId);

        applicationDefinition.setFrom("foilen/fcloud-docker-apache_php:7.2.19-2");

        // Apache and PHP config
        IPApplicationDefinitionAssetsBundle assetsBundle = applicationDefinition.addAssetsBundle();
        assetsBundle.addAssetResource("/etc/apache2/ports.conf", "/com/foilen/infra/resource/urlredirection/apache-ports.conf");
        assetsBundle.addAssetResource("/apache-start.sh", "/com/foilen/infra/resource/urlredirection/apache-start.sh");

        // Site configuration
        Map<String, Object> model = new HashMap<>();
        StringBuilder config = new StringBuilder();
        model.put("logMaxSizeM", 10);
        urlRedirections.forEach(urlRedirection -> {
            model.put("domainName", urlRedirection.getDomainName());
            boolean isPermanent;
            String redirectionUrl;
            if (isHttps) {
                redirectionUrl = urlRedirection.getHttpsRedirectToUrl();
                isPermanent = urlRedirection.isHttpsIsPermanent();
            } else {
                isPermanent = urlRedirection.isHttpIsPermanent();
                redirectionUrl = urlRedirection.getHttpRedirectToUrl();
            }

            // Adjust redirectionUrl
            int endOfProtocolPos = redirectionUrl.indexOf("://");
            boolean redirectionIsExact = false;
            String urlWithoutProtocol = redirectionUrl.substring(endOfProtocolPos + 3);
            String[] parts = urlWithoutProtocol.split("/", 3);
            if (parts.length == 1) {
                // Is just the domain
                while (redirectionUrl.endsWith("//")) {
                    redirectionUrl = redirectionUrl.substring(0, redirectionUrl.length() - 1);
                }
                if (!redirectionUrl.endsWith("/")) {
                    redirectionUrl += "/";
                }

            } else {
                if (!redirectionUrl.endsWith("/")) {
                    redirectionIsExact = true;
                }
            }
            model.put("redirectionUrl", redirectionUrl);
            model.put("redirectionIsExact", redirectionIsExact);

            logger.info("Adding https: {}; isPermanent: {}; model: {}", isHttps, isPermanent, model);
            if (isPermanent) {
                config.append(FreemarkerTools.processTemplate("/com/foilen/infra/resource/urlredirection/apache-http-redirect-permanent.ftl", model));
                config.append("\n");
            } else {
                config.append(FreemarkerTools.processTemplate("/com/foilen/infra/resource/urlredirection/apache-http-redirect-temporary.ftl", model));
                config.append("\n");
            }
        });

        assetsBundle.addAssetContent("/etc/apache2/sites-enabled/000-default.conf", config.toString());

        applicationDefinition.addBuildStepCommand("chmod 644 /etc/apache2/ports.conf ; chmod 755 /apache-start.sh");

        applicationDefinition.addVolume(new IPApplicationDefinitionVolume(null, "/var/lock/apache2", unixUserId, unixUserId, "755"));
        applicationDefinition.addVolume(new IPApplicationDefinitionVolume(null, "/var/log/apache2", unixUserId, unixUserId, "755"));

        applicationDefinition.addContainerUserToChangeId("www-data", unixUserId);

        applicationDefinition.addBuildStepCommand("chmod -R 777 /var/log");
        applicationDefinition.addBuildStepCommand("chown www-data:www-data /var/run/apache2");
        applicationDefinition.addService("apache", "/apache-start.sh");

        applicationDefinition.addPortEndpoint(8080, DockerContainerEndpoints.HTTP_TCP);

        // Update application
        Optional<Application> existingApplication = services.getResourceService().resourceFindByPk(application);
        if (existingApplication.isPresent()) {
            logger.info("Update existing application");
            changes.resourceUpdate(existingApplication.get(), application);
        } else {
            logger.info("Create new application");
            changes.resourceAdd(application);
        }

        // Link machine -> MANAGE-> application
        changes.linkAdd(machine, LinkTypeConstants.MANAGES, application);

        // Link machine
        CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.INSTALLED_ON, Machine.class, Collections.singletonList(machine));

        // Link unix user
        CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.RUN_AS, UnixUser.class, Collections.singletonList(unixUser));

    }

    private void deleteApplicationIfPresent(IPResourceService resourceService, ChangesContext changes, String applicationName) {
        Optional<Application> existingApplication = resourceService.resourceFindByPk(new Application(applicationName));
        if (existingApplication.isPresent()) {
            logger.info("Application {} exists. Deleting it", applicationName);
            changes.resourceDelete(existingApplication.get());
        }
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing machine {}", machineName);

        IPResourceService resourceService = services.getResourceService();
        Optional<Machine> o = resourceService.resourceFindByPk(new Machine(machineName));
        if (!o.isPresent()) {
            logger.info("{} is not present. Skipping", machineName);
            return;
        }
        Machine machine = o.get();

        // Get the unix user
        String unixUserName = UrlRedirectionChangesEventHandler.UNIX_USER_REDIRECTION_NAME;
        UnixUser unixUser;
        Optional<UnixUser> unixUserOptional = resourceService.resourceFind(resourceService.createResourceQuery(UnixUser.class).propertyEquals(UnixUser.PROPERTY_NAME, unixUserName));
        logger.info("Getting unix user {}. Is present: {}", unixUserName, unixUserOptional.isPresent());
        if (!unixUserOptional.isPresent()) {
            logger.info("Deleting any application since the unix user {} is not present", unixUserName);

            deleteApplicationIfPresent(resourceService, changes, "infra_url_redirection_http-" + machineName.replaceAll("\\.", "_"));
            deleteApplicationIfPresent(resourceService, changes, "infra_url_redirection_https-" + machineName.replaceAll("\\.", "_"));
            return;
        }
        unixUser = unixUserOptional.get();

        // Get all the url redirections installed on this machine
        List<UrlRedirection> urlRedirections = resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(UrlRedirection.class, LinkTypeConstants.INSTALLED_ON, machine);
        logger.info("Machine {} has {} Url Redirections", machineName, urlRedirections.size());

        // Apache Applications
        createApacheApplication(services, changes, machine, unixUser, false, urlRedirections.stream() //
                .filter(it -> !Strings.isNullOrEmpty(it.getHttpRedirectToUrl())) //
                .sorted().collect(Collectors.toList()));
        createApacheApplication(services, changes, machine, unixUser, true, urlRedirections.stream() //
                .filter(it -> !Strings.isNullOrEmpty(it.getHttpsRedirectToUrl())) //
                .sorted().collect(Collectors.toList()));

    }

}
