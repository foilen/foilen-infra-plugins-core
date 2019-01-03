/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.unixuser;

import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractUpdateEventHandler;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tuple.Tuple3;

public class SystemUnixUserUpdateHandler extends AbstractUpdateEventHandler<SystemUnixUser> {

    private UnixUserUpdateHandler unixUserUpdateHandler = new UnixUserUpdateHandler();

    @Override
    public void addHandler(CommonServicesContext services, ChangesContext changes, SystemUnixUser resource) {
        unixUserUpdateHandler.addHandler(services, changes, resource);
    }

    @Override
    public void checkAndFix(CommonServicesContext services, ChangesContext changes, SystemUnixUser resource) {
        unixUserUpdateHandler.checkAndFix(services, changes, resource);
    }

    @Override
    public void deleteHandler(CommonServicesContext services, ChangesContext changes, SystemUnixUser resource, List<Tuple3<IPResource, String, IPResource>> previousLinks) {
        unixUserUpdateHandler.deleteHandler(services, changes, resource, previousLinks);
    }

    @Override
    public Class<SystemUnixUser> supportedClass() {
        return SystemUnixUser.class;
    }

    @Override
    public void updateHandler(CommonServicesContext services, ChangesContext changes, SystemUnixUser previousResource, SystemUnixUser newResource) {
        unixUserUpdateHandler.updateHandler(services, changes, previousResource, newResource);
    }

}
