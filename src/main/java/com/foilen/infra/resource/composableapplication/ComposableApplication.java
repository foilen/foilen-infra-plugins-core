/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication;

import java.util.SortedSet;
import java.util.TreeSet;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.website.Website;

/**
 * This is an application that is composed of multiple parts. <br/>
 * Links to:
 * <ul>
 * <li>{@link UnixUser}: (optional / 1) RUN_AS - The default user that executes the services on this application</li>
 * <li>{@link Machine}: (optional / many) INSTALLED_ON - The machines where to install that service</li>
 * <li>{@link AttachablePart}: (optional / many) ATTACHED - The parts to attach</li>
 * </ul>
 *
 * Manages:
 * <ul>
 * <li>{@link Application}: The application with all the parts</li>
 * </ul>
 *
 * Links from:
 * <ul>
 * <li>{@link Website}: (optional / many) POINTS_TO - Websites that will point to the application</li>
 * </ul>
 */
public class ComposableApplication extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Composable Application";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_FROM = "from";
    public static final String PROPERTY_MAIN_COMMAND = "mainCommand";
    public static final String PROPERTY_MAIN_WORKING_DIRECTORY = "mainWorkingDirectory";
    public static final String PROPERTY_ENVIRONMENTS = "environments";
    public static final String PROPERTY_PORTS_EXPOSED_TCP = "portsExposedTcp";
    public static final String PROPERTY_PORTS_EXPOSED_UDP = "portsExposedUdp";
    public static final String PROPERTY_PORTS_ENDPOINT = "portsEndpoint";

    public static final String LINK_TYPE_ATTACHED = "ATTACHED";

    // Details
    private String name;

    // Single
    private String from = "ubuntu:16.04";
    private String mainCommand;
    private String mainWorkingDirectory;
    private SortedSet<String> environments = new TreeSet<>();
    private SortedSet<String> portsExposedTcp = new TreeSet<>();
    private SortedSet<String> portsExposedUdp = new TreeSet<>();
    private SortedSet<String> portsEndpoint = new TreeSet<>();

    public ComposableApplication() {
    }

    /**
     * Primary key.
     *
     * @param name
     *            the name
     */
    public ComposableApplication(String name) {
        this.name = name;
    }

    public SortedSet<String> getEnvironments() {
        return environments;
    }

    public String getFrom() {
        return from;
    }

    public String getMainCommand() {
        return mainCommand;
    }

    public String getMainWorkingDirectory() {
        return mainWorkingDirectory;
    }

    public String getName() {
        return name;
    }

    public SortedSet<String> getPortsEndpoint() {
        return portsEndpoint;
    }

    public SortedSet<String> getPortsExposedTcp() {
        return portsExposedTcp;
    }

    public SortedSet<String> getPortsExposedUdp() {
        return portsExposedUdp;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.INFRASTRUCTURE;
    }

    @Override
    public String getResourceDescription() {
        return "Composable Application";
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public void setEnvironments(SortedSet<String> environments) {
        this.environments = environments;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setMainCommand(String mainCommand) {
        this.mainCommand = mainCommand;
    }

    public void setMainWorkingDirectory(String mainWorkingDirectory) {
        this.mainWorkingDirectory = mainWorkingDirectory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPortsEndpoint(SortedSet<String> portsEndpoint) {
        this.portsEndpoint = portsEndpoint;
    }

    public void setPortsExposedTcp(SortedSet<String> portsExposedTcp) {
        this.portsExposedTcp = portsExposedTcp;
    }

    public void setPortsExposedUdp(SortedSet<String> portsExposedUdp) {
        this.portsExposedUdp = portsExposedUdp;
    }

}
