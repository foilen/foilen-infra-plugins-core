/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.website;

import java.util.List;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractCommonMethodUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.CommonMethodUpdateEventHandlerContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.dns.DnsPointer;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;

public class WebsiteUpdateHandler extends AbstractCommonMethodUpdateEventHandler<Website> {

    public void checkFarLinkChanged(CommonServicesContext services, ChangesContext changes, Machine resource) {
        // NO-OP
    }

    @Override
    protected void commonHandlerExecute(CommonServicesContext services, ChangesContext changes, CommonMethodUpdateEventHandlerContext<Website> context) {

        context.getManagedResourceTypes().add(DnsPointer.class);
        context.getManagedResourceTypes().add(WebsiteCertificate.class);

        IPResourceService resourceService = services.getResourceService();

        Website resource = context.getResource();

        // Create and manage : DnsPointer (attach Machines from the Application)
        List<Machine> installOnMachines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(resource, LinkTypeConstants.INSTALLED_ON, Machine.class);
        for (String domainName : resource.getDomainNames()) {
            DnsPointer dnsPointer = new DnsPointer(domainName);
            dnsPointer = retrieveOrCreateResource(resourceService, changes, dnsPointer, DnsPointer.class);
            updateLinksOnResource(services, changes, dnsPointer, LinkTypeConstants.POINTS_TO, Machine.class, installOnMachines.stream().collect(Collectors.toList()));

            context.getManagedResources().add(dnsPointer);
        }

    }

    @Override
    public Class<Website> supportedClass() {
        return Website.class;
    }

}
