/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractCommonMethodUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.CommonMethodUpdateEventHandlerContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.smalltools.tools.StringTools;

public class MachineUpdateHandler extends AbstractCommonMethodUpdateEventHandler<Machine> {

    @Override
    protected void commonHandlerExecute(CommonServicesContext services, ChangesContext changes, CommonMethodUpdateEventHandlerContext<Machine> context) {

        Machine resource = context.getResource();

        if (context.getOldResource() != null && !StringTools.safeEquals(context.getOldResource().getName(), resource.getName())) {
            throw new IllegalUpdateException("You cannot change a Machine's name");
        }

        context.getManagedResourceTypes().add(DnsEntry.class);

        if (resource.getPublicIp() != null) {
            // Use a DnsEntry
            context.getManagedResources().add(new DnsEntry(resource.getName(), DnsEntryType.A, resource.getPublicIp()));
        }
    }

    @Override
    public Class<Machine> supportedClass() {
        return Machine.class;
    }

}
