/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.resources;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;

/**
 * This is a common Email Server resource. Real implementations will extend this class and use everything linked to it.<br/>
 * Links to:
 * <ul>
 * <li>{@link UnixUser}: (1) RUN_AS - The user that executes the email server</li>
 * <li>{@link Machine}: (optional / many) INSTALLED_ON - The machines where to install that service</li>
 * </ul>
 *
 * Manages:
 * <ul>
 * <li>{@link Application}: The application</li>
 * </ul>
 */
public abstract class EmailServer extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Email Server";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_POSTMASTER_EMAIL = "postmasterEmail";

    // Details
    private String name;
    private String postmasterEmail;

    public EmailServer() {
    }

    /**
     * Primary key.
     *
     * @param name
     *            the name
     */
    public EmailServer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPostmasterEmail() {
        return postmasterEmail;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.EMAIL;
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPostmasterEmail(String postmasterEmail) {
        this.postmasterEmail = postmasterEmail;
    }

}
