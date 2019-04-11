/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.backup.handlers;

import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.resource.backup.resources.BackupToSftpConfig;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.smalltools.tuple.Tuple3;

public class BackupToSftpEventHandler extends AbstractUpdateEventHandler<BackupToSftpConfig> {

    @Override
    public void addHandler(CommonServicesContext services, ChangesContext changes, BackupToSftpConfig resource) {
        commonHandlerExecute(services, changes);
    }

    @Override
    public void checkAndFix(CommonServicesContext services, ChangesContext changes, BackupToSftpConfig resource) {
        commonHandlerExecute(services, changes);
    }

    protected void commonHandlerExecute(CommonServicesContext services, ChangesContext changes) {
        IPResourceService resourceService = services.getResourceService();
        resourceService.resourceFindAll(resourceService.createResourceQuery(Machine.class)).forEach(machine -> changes.resourceRefresh(machine));
    }

    @Override
    public void deleteHandler(CommonServicesContext services, ChangesContext changes, BackupToSftpConfig resource, List<Tuple3<IPResource, String, IPResource>> previousLinks) {
        commonHandlerExecute(services, changes);
    }

    @Override
    public Class<BackupToSftpConfig> supportedClass() {
        return BackupToSftpConfig.class;
    }

    @Override
    public void updateHandler(CommonServicesContext services, ChangesContext changes, BackupToSftpConfig previousResource, BackupToSftpConfig newResource) {
        commonHandlerExecute(services, changes);
    }

}
