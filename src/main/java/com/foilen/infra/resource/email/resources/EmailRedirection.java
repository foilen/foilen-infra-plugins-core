/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.resources;

import java.util.Set;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.google.common.base.Joiner;

/**
 * This is a redirection on a domain.<br/>
 * Links to:
 * <ul>
 * <li>{@link EmailDomain}: (optional / many) INSTALLED_ON - The email domains that manages that account</li>
 * </ul>
 */
public class EmailRedirection extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Email Redirection";

    public static final String PROPERTY_UID = "uid";
    public static final String PROPERTY_ACCOUNT_NAME = "accountName";
    public static final String PROPERTY_REDIRECT_TOS = "redirectTos";

    // Details
    private String uid;
    private String accountName;
    private Set<String> redirectTos;

    public EmailRedirection() {
        uid = SecureRandomTools.randomBase64String(10);
    }

    public String getAccountName() {
        return accountName;
    }

    public Set<String> getRedirectTos() {
        return redirectTos;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.EMAIL;
    }

    @Override
    public String getResourceDescription() {
        return "Redirect to: " + Joiner.on(", ").join(redirectTos);
    }

    @Override
    public String getResourceName() {
        return accountName;
    }

    public String getUid() {
        return uid;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setRedirectTos(Set<String> redirectTos) {
        this.redirectTos = redirectTos;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
