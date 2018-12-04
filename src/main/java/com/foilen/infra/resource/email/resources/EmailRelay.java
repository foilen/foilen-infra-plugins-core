/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.resources;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;

public class EmailRelay extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Email Relay";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_HOSTNAME = "hostname";
    public static final String PROPERTY_PORT = "port";
    public static final String PROPERTY_USERNAME = "username";
    public static final String PROPERTY_PASSWORD = "password";

    // Details
    private String name;
    private String hostname;
    private Integer port;
    private String username;
    private String password;

    public String getHostname() {
        return hostname;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.EMAIL;
    }

    @Override
    public String getResourceDescription() {
        return hostname + ":" + port + " " + username;
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
