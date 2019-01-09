/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.smalltools.tools.SecureRandomTools;

/**
 * This is the details of a credential.
 */
public class ApachePhpHtPasswd extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Apache and PHP - HT Passwd";

    public static final String PROPERTY_UID = "uid";
    public static final String PROPERTY_USER = "user";
    public static final String PROPERTY_PASSWORD = "password";

    // Details
    private String uid = SecureRandomTools.randomBase64String(10);
    private String user;
    private String password;

    public ApachePhpHtPasswd() {
    }

    public ApachePhpHtPasswd(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.NET;
    }

    @Override
    public String getResourceDescription() {
        return user;
    }

    @Override
    public String getResourceName() {
        return uid;
    }

    public String getUid() {
        return uid;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
