/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.resources;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.smalltools.tools.SecureRandomTools;

/**
 * This is an account on a domain.<br/>
 * Links to:
 * <ul>
 * <li>{@link EmailDomain}: (optional / many) INSTALLED_ON - The email domains that manages that account</li>
 * </ul>
 */
public class EmailAccount extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Email Account";

    public static final String PROPERTY_UID = "uid";
    public static final String PROPERTY_ACCOUNT_NAME = "accountName";
    public static final String PROPERTY_SHA_512_PASSWORD = "sha512Password";

    // Details
    private String uid;
    private String accountName;
    private String sha512Password;

    public EmailAccount() {
        uid = SecureRandomTools.randomBase64String(10);
    }

    public String getAccountName() {
        return accountName;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.EMAIL;
    }

    @Override
    public String getResourceDescription() {
        return "Email Account";
    }

    @Override
    public String getResourceName() {
        return accountName;
    }

    public String getSha512Password() {
        return sha512Password;
    }

    public String getUid() {
        return uid;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setSha512Password(String sha512Password) {
        this.sha512Password = sha512Password;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
