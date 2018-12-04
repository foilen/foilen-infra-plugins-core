/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9;

import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.resource.dns.DnsEntry;
import com.foilen.smalltools.tuple.Tuple3;

public class Bind9DnsEntryUpdateEventHandler extends AbstractUpdateEventHandler<DnsEntry> {

    @Override
    public void addHandler(CommonServicesContext services, ChangesContext changes, DnsEntry resource) {
        common(services, changes);
    }

    @Override
    public void checkAndFix(CommonServicesContext services, ChangesContext changes, DnsEntry resource) {
    }

    private void common(CommonServicesContext services, ChangesContext changes) {
        IPResourceService resourceService = services.getResourceService();
        List<Bind9Server> bind9Servers = resourceService.resourceFindAll(resourceService.createResourceQuery(Bind9Server.class));
        bind9Servers.forEach(it -> {
            changes.resourceRefresh(it);
        });
    }

    @Override
    public void deleteHandler(CommonServicesContext services, ChangesContext changes, DnsEntry resource, List<Tuple3<IPResource, String, IPResource>> previousLinks) {
        common(services, changes);
    }

    @Override
    public Class<DnsEntry> supportedClass() {
        return DnsEntry.class;
    }

    @Override
    public void updateHandler(CommonServicesContext services, ChangesContext changes, DnsEntry previousResource, DnsEntry newResource) {
        common(services, changes);
    }

}
