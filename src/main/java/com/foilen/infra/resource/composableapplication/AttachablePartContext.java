/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.resource.application.Application;

public class AttachablePartContext {

    private CommonServicesContext services;

    private Application application;
    private IPApplicationDefinition applicationDefinition;

    public Application getApplication() {
        return application;
    }

    public IPApplicationDefinition getApplicationDefinition() {
        return applicationDefinition;
    }

    public CommonServicesContext getServices() {
        return services;
    }

    public AttachablePartContext setApplication(Application application) {
        this.application = application;
        return this;
    }

    public AttachablePartContext setApplicationDefinition(IPApplicationDefinition applicationDefinition) {
        this.applicationDefinition = applicationDefinition;
        return this;
    }

    public AttachablePartContext setServices(CommonServicesContext services) {
        this.services = services;
        return this;
    }

}
