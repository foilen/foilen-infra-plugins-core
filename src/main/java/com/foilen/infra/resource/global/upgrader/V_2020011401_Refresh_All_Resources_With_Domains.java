/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.global.upgrader;

import java.util.ArrayList;
import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.bind9.Bind9Server;
import com.foilen.infra.resource.dns.DnsEntry;
import com.foilen.infra.resource.dns.DnsPointer;
import com.foilen.infra.resource.email.resources.EmailDomain;
import com.foilen.infra.resource.global.AbstractPluginUpgraderTask;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.urlredirection.UrlRedirection;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;

public class V_2020011401_Refresh_All_Resources_With_Domains extends AbstractPluginUpgraderTask {

    @Override
    public void execute() {

        IPResourceService resourceService = commonServicesContext.getResourceService();

        List<Class<? extends IPResource>> resourceTypes = new ArrayList<>();
        resourceTypes.add(Application.class);
        resourceTypes.add(Bind9Server.class);
        resourceTypes.add(DnsEntry.class);
        resourceTypes.add(DnsPointer.class);
        resourceTypes.add(EmailDomain.class);
        resourceTypes.add(Machine.class);
        resourceTypes.add(UrlRedirection.class);
        resourceTypes.add(Website.class);
        resourceTypes.add(WebsiteCertificate.class);

        resourceTypes.forEach(resourceType -> {
            ChangesContext changes = new ChangesContext(resourceService);
            resourceService.resourceFindAll(resourceService.createResourceQuery(resourceType)).stream() //
                    .forEach(it -> {
                        logger.info("Refresh {}", it);
                        changes.resourceRefresh(it);
                    });
            if (changes.hasChanges()) {
                internalServicesContext.getInternalChangeService().changesExecute(changes);
            }
        });

    }

    @Override
    public String useTracker() {
        return UpgradeTask.DEFAULT_TRACKER;
    }

}
