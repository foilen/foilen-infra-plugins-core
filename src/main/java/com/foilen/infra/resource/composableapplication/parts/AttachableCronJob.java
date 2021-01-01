/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.parts;

import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.application.model.ExecutionPolicy;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.AttachablePartContext;

/**
 * This is for a Cron Job. <br>
 * Links to:
 * <ul>
 * <li>None</li>
 * </ul>
 */
@Deprecated
public class AttachableCronJob extends AttachablePart {

    public static final String RESOURCE_TYPE = "Attachable Cron Job";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_TIME = "time";

    // Basics
    private String name;
    private String description;
    private String time;

    public AttachableCronJob() {
    }

    @Override
    public void attachTo(AttachablePartContext context) {

        Application application = context.getApplication();
        application.setExecutionPolicy(ExecutionPolicy.CRON);
        application.setExecutionCronDetails(time);

    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.INFRASTRUCTURE;
    }

    @Override
    public String getResourceDescription() {
        return description;
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
