/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

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
import com.foilen.infra.resource.postgresql.PostgreSqlServer;

/**
 * This is to add a local redirection to a {@link PostgreSqlServer}. <br/>
 * Links to:
 * <ul>
 * <li>{@link PostgreSqlServer}: (1) POINTS_TO - The PostgreSql DB to use</li>
 * </ul>
 */
public class AttachablePostgreSql extends AttachablePart {

    public static final String RESOURCE_TYPE = "Attachable PostgreSql";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_LOCAL_PORT = "localPort";

    private String name;
    private int localPort = 5432;

    @Override
    public void attachTo(AttachablePartContext context) {

        // Get the PostgreSqlServer (fail if more than one)
        CommonServicesContext services = context.getServices();
        List<PostgreSqlServer> servers = services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(this, LinkTypeConstants.POINTS_TO, PostgreSqlServer.class);
        if (servers.size() > 1) {
            throw new IllegalUpdateException("There cannot be more than 1 PostgreSql Server. Has " + servers.size());
        }
        if (servers.isEmpty()) {
            return;
        }
        PostgreSqlServer server = servers.get(0);

        // Get the Machines on the PostgreSqlServer
        List<Machine> machines = services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(server, LinkTypeConstants.INSTALLED_ON, Machine.class);
        if (machines.isEmpty()) {
            return;
        }

        // Add the infra on the Application
        Machine machine = machines.get(0);
        context.getApplicationDefinition().addPortRedirect(localPort, machine.getName(), server.getName(), "POSTGRESQL_TCP");
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
        return "Local infra to a PostgreSql Database";
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
