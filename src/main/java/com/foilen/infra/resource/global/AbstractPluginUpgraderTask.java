/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.global;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;

public abstract class AbstractPluginUpgraderTask extends AbstractBasics implements UpgradeTask {

    protected CommonServicesContext commonServicesContext;
    protected InternalServicesContext internalServicesContext;

    public void setCommonServicesContext(CommonServicesContext commonServicesContext) {
        this.commonServicesContext = commonServicesContext;
    }

    public void setInternalServicesContext(InternalServicesContext internalServicesContext) {
        this.internalServicesContext = internalServicesContext;
    }

    @Override
    public String useTracker() {
        return UpgradeTask.DEFAULT_TRACKER;
    }

}
