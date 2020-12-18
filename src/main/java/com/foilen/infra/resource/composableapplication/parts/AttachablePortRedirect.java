/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.parts;

import java.util.List;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.AttachablePartContext;
import com.foilen.infra.resource.machine.Machine;
import com.google.common.base.Strings;

/**
 * This is to add a local redirection to an {@link Application}. <br/>
 * Links to:
 * <ul>
 * <li>{@link Application}: (1) POINTS_TO - The Application to redirect</li>
 * </ul>
 */
public class AttachablePortRedirect extends AttachablePart {

    public static final String RESOURCE_TYPE = "Attachable Port Redirect";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_LOCAL_PORT = "localPort";
    public static final String PROPERTY_TO_ENDPOINT = "toEndpoint";

    private String name;
    private int localPort = 9999;
    private String toEndpoint;

    public AttachablePortRedirect() {
    }

    /**
     * Primary key.
     *
     * @param name
     *            the name
     */
    public AttachablePortRedirect(String name) {
        this.name = name;
    }

    public AttachablePortRedirect(String name, int localPort, String toEndpoint) {
        this.name = name;
        this.localPort = localPort;
        this.toEndpoint = toEndpoint;
    }

    @Override
    public void attachTo(AttachablePartContext context) {

        if (Strings.isNullOrEmpty(toEndpoint)) {
            return;
        }

        // Get the Application (fail if more than one)
        CommonServicesContext services = context.getServices();
        List<Application> applications = services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(this, LinkTypeConstants.POINTS_TO, Application.class);
        if (applications.size() > 1) {
            throw new IllegalUpdateException("There cannot be more than 1 Application. Has " + applications.size());
        }
        if (applications.isEmpty()) {
            return;
        }
        Application application = applications.get(0);

        // Get the Machines on the Application
        List<Machine> machines = services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(application, LinkTypeConstants.INSTALLED_ON, Machine.class);
        if (machines.isEmpty()) {
            return;
        }

        // Add the infra on the Application
        Machine machine = machines.get(0);
        context.getApplicationDefinition().addPortRedirect(localPort, machine.getName(), application.getName(), toEndpoint);
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getName() {
        return name;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.NET;
    }

    @Override
    public String getResourceDescription() {
        return "Local infra to an Application";
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public String getToEndpoint() {
        return toEndpoint;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setToEndpoint(String toEndpoint) {
        this.toEndpoint = toEndpoint;
    }

}
