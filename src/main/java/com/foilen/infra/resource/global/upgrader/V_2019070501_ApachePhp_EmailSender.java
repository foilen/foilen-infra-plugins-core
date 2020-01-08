/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.global.upgrader;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.apachephp.ApachePhp;
import com.foilen.infra.resource.apachephp.EmailSender;
import com.foilen.infra.resource.global.AbstractPluginUpgraderTask;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;

public class V_2019070501_ApachePhp_EmailSender extends AbstractPluginUpgraderTask {

    @SuppressWarnings("deprecation")
    @Override
    public void execute() {

        IPResourceService resourceService = commonServicesContext.getResourceService();

        ChangesContext changes = new ChangesContext(resourceService);

        resourceService.resourceFindAll(resourceService.createResourceQuery(ApachePhp.class)).stream() //
                .forEach(it -> {
                    it.setEmailSender(it.isEmailSenderMsmtp() ? EmailSender.MSMTP : EmailSender.SENDMAIL);
                    changes.resourceUpdate(it);
                });

        internalServicesContext.getInternalChangeService().changesExecute(changes);

    }

    @Override
    public String useTracker() {
        return UpgradeTask.DEFAULT_TRACKER;
    }

}
