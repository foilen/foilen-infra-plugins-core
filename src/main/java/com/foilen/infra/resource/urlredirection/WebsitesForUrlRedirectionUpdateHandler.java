/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.urlredirection;

import java.util.List;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractFinalStateManagedResourcesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.FinalStateManagedResource;
import com.foilen.infra.plugin.v1.core.eventhandler.FinalStateManagedResourcesUpdateEventHandlerContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.infra.resource.website.Website;
import com.google.common.base.Strings;

/**
 * For each UrlRedirection:
 * <ul>
 * <li>Create 1 Website for http redirection</li>
 * <li>Create 1 Website for https redirection</li>
 * </ul>
 *
 * They will point to the http or https Application managed by RedirectionWithApacheUpdateHandler.
 */
public class WebsitesForUrlRedirectionUpdateHandler extends AbstractFinalStateManagedResourcesEventHandler<UrlRedirection> {

    @Override
    protected void commonHandlerExecute(CommonServicesContext services, FinalStateManagedResourcesUpdateEventHandlerContext<UrlRedirection> context) {

        UrlRedirection urlRedirection = context.getResource();
        logger.info("Processing {}", urlRedirection);

        context.addManagedResourceTypes(Website.class);

        IPResourceService resourceService = services.getResourceService();
        String domainName = urlRedirection.getDomainName();

        // Find the machines
        List<Machine> machines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(urlRedirection, LinkTypeConstants.INSTALLED_ON, Machine.class);
        if (machines.isEmpty()) {
            logger.info("No machines. Skipping");
            return;
        }
        logger.info("Got {} machines", machines.size());

        // Apply HTTP
        if (!Strings.isNullOrEmpty(urlRedirection.getHttpRedirectToUrl())) {
            createWebsite("HTTP", context, resourceService, domainName, machines);
        }

        // Apply HTTPS
        if (!Strings.isNullOrEmpty(urlRedirection.getHttpsRedirectToUrl())) {

            List<WebsiteCertificate> websiteCertificates = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(urlRedirection, LinkTypeConstants.USES, WebsiteCertificate.class);
            if (websiteCertificates.isEmpty()) {
                logger.info("No website certificate. Skipping");
                return;
            }

            FinalStateManagedResource websiteFinalState = createWebsite("HTTPS", context, resourceService, domainName, machines);

            // WebsiteCertificate
            websiteCertificates.forEach(websiteCertificate -> {
                websiteFinalState.addLinkTo(LinkTypeConstants.USES, websiteCertificate);
            });
        }

    }

    private FinalStateManagedResource createWebsite(String protocol, FinalStateManagedResourcesUpdateEventHandlerContext<UrlRedirection> context, IPResourceService resourceService, String domainName,
            List<Machine> machines) {

        Website website = new Website(protocol + " Redirection of " + domainName);
        FinalStateManagedResource websiteFinalResource = new FinalStateManagedResource();
        websiteFinalResource.setManagedResource(website);
        context.addManagedResources(websiteFinalResource);
        website.getDomainNames().add(domainName);
        website.setHttps("HTTPS".equals(protocol));

        websiteFinalResource.addManagedLinksToType(LinkTypeConstants.POINTS_TO, LinkTypeConstants.INSTALLED_ON, LinkTypeConstants.USES);

        machines.forEach(machine -> {

            // Machine
            websiteFinalResource.addLinkTo(LinkTypeConstants.INSTALLED_ON, machine);

            // Applications
            String applicationName = "infra_url_redirection_" + protocol.toLowerCase() + "-" + machine.getName().replaceAll("\\.", "_");
            logger.info("Getting application {}", applicationName);
            Optional<Application> application = resourceService.resourceFind(resourceService.createResourceQuery(Application.class) //
                    .propertyEquals(Application.PROPERTY_NAME, applicationName) //
            );
            if (!application.isPresent()) {
                logger.info("Application {} does not exist for now. Skipping", applicationName);
                return;
            }
            websiteFinalResource.addLinkTo(LinkTypeConstants.POINTS_TO, application.get());

        });

        return websiteFinalResource;
    }

    @Override
    public Class<UrlRedirection> supportedClass() {
        return UrlRedirection.class;
    }

}
