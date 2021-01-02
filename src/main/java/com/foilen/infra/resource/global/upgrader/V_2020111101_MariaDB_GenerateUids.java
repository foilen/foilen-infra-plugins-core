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
import com.foilen.infra.resource.global.AbstractPluginUpgraderTask;
import com.foilen.infra.resource.mariadb.MariaDBDatabase;
import com.foilen.infra.resource.mariadb.MariaDBUser;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;

public class V_2020111101_MariaDB_GenerateUids extends AbstractPluginUpgraderTask {

    @Override
    public void execute() {

        IPResourceService resourceService = commonServicesContext.getResourceService();

        List<Class<? extends IPResource>> resourceTypes = new ArrayList<>();
        resourceTypes.add(MariaDBDatabase.class);
        resourceTypes.add(MariaDBUser.class);

        ChangesContext changes = new ChangesContext(resourceService);
        resourceTypes.forEach(resourceType -> {
            resourceService.resourceFindAll(resourceService.createResourceQuery(resourceType)).stream() //
                    .forEach(it -> {
                        logger.info("Generate uids {}", it);
                        changes.resourceUpdate(it);
                    });
        });
        if (changes.hasChanges()) {
            internalServicesContext.getInternalChangeService().changesExecute(changes);
        }

    }

    @Override
    public String useTracker() {
        return UpgradeTask.DEFAULT_TRACKER;
    }

}
