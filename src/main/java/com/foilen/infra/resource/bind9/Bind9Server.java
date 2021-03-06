/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9;

import java.util.SortedSet;
import java.util.TreeSet;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.domain.Domain;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;

/**
 * This is a DNS Server. <br/>
 * Links to:
 * <ul>
 * <li>{@link UnixUser}: (optional / 1) RUN_AS - The user that executes that server</li>
 * <li>{@link Machine}: (optional / many) INSTALLED_ON - The machines where to install that server</li>
 * </ul>
 *
 * Manages:
 * <ul>
 * <li>{@link Domain}: (optional / many) MANAGES - The domains</li>
 * <li>{@link Application}: The Bind9 application</li>
 * </ul>
 */
public class Bind9Server extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Bind9 Server";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_PORT = "port";
    public static final String PROPERTY_NS_DOMAIN_NAMES = "nsDomainNames";
    public static final String PROPERTY_ADMIN_EMAIL = "adminEmail";

    // Details
    private String name;
    private Integer port = 53;
    private String adminEmail;

    // Network
    private SortedSet<String> nsDomainNames = new TreeSet<>();

    public Bind9Server() {
    }

    /**
     * Primary key.
     *
     * @param name
     *            the name
     */
    public Bind9Server(String name) {
        this.name = name;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public String getName() {
        return name;
    }

    public SortedSet<String> getNsDomainNames() {
        return nsDomainNames;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.NET;
    }

    @Override
    public String getResourceDescription() {
        return "DNS Server";
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public Bind9Server setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
        return this;
    }

    public Bind9Server setName(String name) {
        this.name = name;
        return this;
    }

    public Bind9Server setNsDomainNames(SortedSet<String> nsDomainNames) {
        this.nsDomainNames = nsDomainNames;
        return this;
    }

    public Bind9Server setPort(Integer port) {
        this.port = port;
        return this;
    }

}
