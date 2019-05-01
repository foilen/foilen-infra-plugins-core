/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.urlredirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;
import com.google.common.base.Strings;

/**
 * For each UrlRedirection:
 * <ul>
 * <li>Create 1 Website for http redirection</li>
 * <li>Create 1 Website for https redirection</li>
 * </ul>
 *
 * They will point to the http or https Application managed by UrlRedirectionManageApplicationActionHandler.
 */
public class UrlRedirectionManageWebsitesActionHandler extends AbstractBasics implements ActionHandler {

    private String urlRedirectionDomainName;

    public UrlRedirectionManageWebsitesActionHandler(String urlRedirectionDomainName) {
        this.urlRedirectionDomainName = urlRedirectionDomainName;
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing {}", urlRedirectionDomainName);

        IPResourceService resourceService = services.getResourceService();
        Optional<UrlRedirection> o = resourceService.resourceFindByPk(new UrlRedirection(urlRedirectionDomainName));
        if (!o.isPresent()) {
            logger.info("{} is not present. Skipping", urlRedirectionDomainName);
            return;
        }
        UrlRedirection urlRedirection = o.get();

        List<Machine> machines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(urlRedirection, LinkTypeConstants.INSTALLED_ON, Machine.class);
        List<WebsiteCertificate> websiteCertificates = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(urlRedirection, LinkTypeConstants.USES, WebsiteCertificate.class);

        List<Website> desiredWebsites = new ArrayList<>();

        String domainName = urlRedirection.getDomainName();
        // Apply HTTP
        if (!Strings.isNullOrEmpty(urlRedirection.getHttpRedirectToUrl())) {
            desiredWebsites.add(getOrCreateWebsite("HTTP", resourceService, domainName, machines, changes));
        }

        // Apply HTTPS
        if (!Strings.isNullOrEmpty(urlRedirection.getHttpsRedirectToUrl())) {

            if (websiteCertificates.isEmpty()) {
                logger.info("No website certificate. Skipping");
                return;
            }

            Website httpsWebsite = getOrCreateWebsite("HTTPS", resourceService, domainName, machines, changes);
            desiredWebsites.add(httpsWebsite);

            // website -> USES -> WebsiteCertificate
            CommonResourceLink.syncToLinks(services, changes, httpsWebsite, LinkTypeConstants.USES, WebsiteCertificate.class, websiteCertificates);
        }

        CommonResourceLink.syncToLinks(services, changes, urlRedirection, LinkTypeConstants.MANAGES, Website.class, desiredWebsites);

    }

    private Website getOrCreateWebsite(String protocol, IPResourceService resourceService, String domainName, List<Machine> machines, ChangesContext changes) {

        Website website = new Website(protocol + " Redirection of " + domainName);

        website.getDomainNames().add(domainName);
        website.setHttps("HTTPS".equals(protocol));

        // Create or update
        Optional<Website> existingWebsite = resourceService.resourceFindByPk(website);
        if (existingWebsite.isPresent()) {
            if (!StringTools.safeEquals(JsonTools.compactPrintWithoutNulls(website), JsonTools.compactPrintWithoutNulls(existingWebsite.get()))) {
                changes.resourceUpdate(existingWebsite.get(), website);
            }
            website = existingWebsite.get();
        } else {
            changes.resourceAdd(website);
        }

        // website -> INSTALLED_ON -> Machines
        CommonResourceLink.syncToLinks(resourceService, changes, website, LinkTypeConstants.INSTALLED_ON, Machine.class, machines);

        List<Application> applications = new ArrayList<>();
        machines.forEach(machine -> {

            // Applications
            String applicationName = "infra_url_redirection_" + protocol.toLowerCase() + "-" + machine.getName().replaceAll("\\.", "_");
            logger.info("Getting application {}", applicationName);
            Optional<Application> existingApplication = resourceService.resourceFind(resourceService.createResourceQuery(Application.class) //
                    .propertyEquals(Application.PROPERTY_NAME, applicationName) //
            );
            if (existingApplication.isPresent()) {
                logger.info("Application {} exists. Using it", applicationName);
                applications.add(existingApplication.get());
            } else {
                logger.info("Application {} does not exist. Creating an empty one", applicationName);
                Application emptyApplication = new Application(applicationName);
                applications.add(emptyApplication);
                changes.resourceAdd(emptyApplication);
            }

        });

        // website -> POINTS_TO -> Applications
        CommonResourceLink.syncToLinks(resourceService, changes, website, LinkTypeConstants.POINTS_TO, Application.class, applications);

        return website;
    }

}
