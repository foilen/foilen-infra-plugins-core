/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.global.upgrader;

import java.util.ArrayList;
import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.resource.apachephp.ApachePhp;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.bind9.Bind9Server;
import com.foilen.infra.resource.composableapplication.ComposableApplication;
import com.foilen.infra.resource.dns.DnsPointer;
import com.foilen.infra.resource.email.resources.JamesEmailServer;
import com.foilen.infra.resource.global.AbstractPluginUpgraderTask;
import com.foilen.infra.resource.infraconfig.InfraConfig;
import com.foilen.infra.resource.letsencrypt.plugin.LetsencryptConfig;
import com.foilen.infra.resource.mariadb.MariaDBServer;
import com.foilen.infra.resource.mongodb.MongoDBServer;
import com.foilen.infra.resource.postgresql.PostgreSqlServer;
import com.foilen.infra.resource.urlredirection.UrlRedirection;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;

public class V_2019061201_Refresh extends AbstractPluginUpgraderTask {

    @Override
    public void execute() {

        IPResourceService resourceService = commonServicesContext.getResourceService();

        List<Class<? extends IPResource>> resourceTypes = new ArrayList<>();
        resourceTypes.add(ApachePhp.class);
        resourceTypes.add(Application.class);
        resourceTypes.add(Bind9Server.class);
        resourceTypes.add(ComposableApplication.class);
        resourceTypes.add(DnsPointer.class);
        resourceTypes.add(InfraConfig.class);
        resourceTypes.add(JamesEmailServer.class);
        resourceTypes.add(LetsencryptConfig.class);
        resourceTypes.add(MariaDBServer.class);
        resourceTypes.add(MongoDBServer.class);
        resourceTypes.add(PostgreSqlServer.class);
        resourceTypes.add(UrlRedirection.class);
        resourceTypes.add(Website.class);

        resourceTypes.forEach(resourceType -> {
            ChangesContext changes = new ChangesContext(resourceService);
            resourceService.resourceFindAll(resourceService.createResourceQuery(resourceType)).stream() //
                    .forEach(it -> changes.resourceRefresh(it));
            internalServicesContext.getInternalChangeService().changesExecute(changes);
        });

    }

    @Override
    public String useTracker() {
        return UpgradeTask.DEFAULT_TRACKER;
    }

}
