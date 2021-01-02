/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.global.upgrader;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.apachephp.ApachePhp;
import com.foilen.infra.resource.global.AbstractPluginUpgraderTask;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;

public class V_2020071401_ApachePhp_Set_Memory_Limit extends AbstractPluginUpgraderTask {

    @Override
    public void execute() {

        IPResourceService resourceService = commonServicesContext.getResourceService();

        ChangesContext changes = new ChangesContext(resourceService);
        resourceService.resourceFindAll(resourceService.createResourceQuery(ApachePhp.class)).stream() //
                .forEach(it -> {
                    logger.info("Set max memory for {}", it);
                    it.setMaxMemoryM(it.getMaxUploadFilesizeM() * 3);
                    changes.resourceUpdate(it);
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
