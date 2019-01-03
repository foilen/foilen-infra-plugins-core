/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractCommonMethodUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.CommonMethodUpdateEventHandlerContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.infra.resource.machine.Machine;
import com.google.common.base.Strings;

public class DnsPointerUpdateHandler extends AbstractCommonMethodUpdateEventHandler<DnsPointer> {

    @Override
    protected void commonHandlerExecute(CommonServicesContext services, ChangesContext changes, CommonMethodUpdateEventHandlerContext<DnsPointer> context) {

        IPResourceService resourceService = services.getResourceService();

        DnsPointer resource = context.getResource();
        logger.debug("Pointer {}", resource.getName());

        context.getManagedResourceTypes().add(DnsEntry.class);

        // Use a DnsEntry per machine
        List<Machine> machines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(resource, LinkTypeConstants.POINTS_TO, Machine.class);
        logger.debug("{} points to {} machines", resource.getName(), machines.size());

        for (Machine machine : machines) {
            if (Strings.isNullOrEmpty(machine.getPublicIp())) {
                logger.debug("{} ignoring DnsEntry {} : no public ip", resource.getName(), machine.getName());
            } else {
                logger.debug("{} adding DnsEntry {}", resource.getName(), machine.getPublicIp());
                context.getManagedResources().add(new DnsEntry(resource.getName(), DnsEntryType.A, machine.getPublicIp()));
            }
        }

    }

    @Override
    public Class<DnsPointer> supportedClass() {
        return DnsPointer.class;
    }

}
