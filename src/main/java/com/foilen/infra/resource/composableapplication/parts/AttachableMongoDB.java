/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.parts;

import java.util.List;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.AttachablePartContext;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.mongodb.MongoDBServer;

/**
 * This is to add a local redirection to a {@link MongoDBServer}. <br/>
 * Links to:
 * <ul>
 * <li>{@link MongoDBServer}: (1) POINTS_TO - The MongoDB to use</li>
 * </ul>
 */
public class AttachableMongoDB extends AttachablePart {

    public static final String RESOURCE_TYPE = "Attachable MongoDB";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_LOCAL_PORT = "localPort";

    private String name;
    private int localPort = 27017;

    @Override
    public void attachTo(AttachablePartContext context) {

        // Get the MongoDBServer (fail if more than one)
        CommonServicesContext services = context.getServices();
        List<MongoDBServer> servers = services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(this, LinkTypeConstants.POINTS_TO, MongoDBServer.class);
        if (servers.size() > 1) {
            throw new IllegalUpdateException("There cannot be more than 1 MongoDB Server. Has " + servers.size());
        }
        if (servers.isEmpty()) {
            return;
        }
        MongoDBServer server = servers.get(0);

        // Get the Machines on the MongoDBServer
        List<Machine> machines = services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(server, LinkTypeConstants.INSTALLED_ON, Machine.class);
        if (machines.isEmpty()) {
            return;
        }

        // Add the infra on the Application
        Machine machine = machines.get(0);
        context.getApplicationDefinition().addPortRedirect(localPort, machine.getName(), server.getName(), "MONGODB_TCP");
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getName() {
        return name;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.DATABASE;
    }

    @Override
    public String getResourceDescription() {
        return "Local infra to a MongoDB Database";
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public void setName(String name) {
        this.name = name;
    }

}
