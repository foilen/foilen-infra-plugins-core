/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.global;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.upgrader.trackers.UpgraderTracker;

public class UpgraderItemUpgraderTracker extends AbstractBasics implements UpgraderTracker {

    private IPResourceService resourceService;
    private InternalChangeService internalChangeService;
    private UpgraderItem upgraderItem;

    public UpgraderItemUpgraderTracker(IPResourceService resourceService, InternalChangeService internalChangeService, UpgraderItem upgraderItem) {
        this.resourceService = resourceService;
        this.internalChangeService = internalChangeService;
        this.upgraderItem = upgraderItem;
    }

    @Override
    public void executionBegin(String taskSimpleName) {
    }

    @Override
    public void executionEnd(String taskSimpleName, boolean isSuccessful) {
        if (isSuccessful) {
            upgraderItem.getApplied().add(taskSimpleName);
            ChangesContext changes = new ChangesContext(resourceService);
            changes.resourceUpdate(upgraderItem);
            internalChangeService.changesExecute(changes);
        }
    }

    @Override
    public void trackerBegin() {
    }

    @Override
    public void trackerEnd() {
    }

    @Override
    public boolean wasExecutedSuccessfully(String taskSimpleName) {
        return upgraderItem.getApplied().contains(taskSimpleName);
    }

}
