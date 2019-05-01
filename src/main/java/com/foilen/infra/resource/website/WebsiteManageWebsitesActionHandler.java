/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.website;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.dns.DnsPointer;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;

/**
 * For each Website:
 * <ul>
 * <li>Create 1 DnsPointer per domain name</li>
 * </ul>
 *
 * It will point to the machines on which the website is hosted.
 */
public class WebsiteManageWebsitesActionHandler extends AbstractBasics implements ActionHandler {

    private String websiteName;

    public WebsiteManageWebsitesActionHandler(String websiteName) {
        this.websiteName = websiteName;
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing {}", websiteName);

        IPResourceService resourceService = services.getResourceService();

        Optional<Website> o = resourceService.resourceFindByPk(new Website(websiteName));
        if (!o.isPresent()) {
            logger.info("{} is not present. Skipping", websiteName);
            return;
        }
        Website website = o.get();

        List<DnsPointer> desiredDnsPointers = new ArrayList<>();

        // Create and manage : DnsPointer (attach Machines from the Application)
        List<Machine> installOnMachines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(website, LinkTypeConstants.INSTALLED_ON, Machine.class);
        for (String domainName : website.getDomainNames()) {
            DnsPointer dnsPointer = new DnsPointer(domainName);

            // Create or update
            Optional<DnsPointer> existingDnsPointer = resourceService.resourceFindByPk(dnsPointer);
            if (existingDnsPointer.isPresent()) {
                if (!StringTools.safeEquals(JsonTools.compactPrintWithoutNulls(dnsPointer), JsonTools.compactPrintWithoutNulls(existingDnsPointer.get()))) {
                    changes.resourceUpdate(existingDnsPointer.get(), dnsPointer);
                }
                dnsPointer = existingDnsPointer.get();
            } else {
                changes.resourceAdd(dnsPointer);
            }
            desiredDnsPointers.add(dnsPointer);

            // dnsPointer -> POINTS_TO -> installOnMachines
            CommonResourceLink.syncToLinks(services, changes, dnsPointer, LinkTypeConstants.POINTS_TO, Machine.class, installOnMachines);

        }

        CommonResourceLink.syncToLinks(services, changes, website, LinkTypeConstants.MANAGES, DnsPointer.class, desiredDnsPointers);

    }

}
