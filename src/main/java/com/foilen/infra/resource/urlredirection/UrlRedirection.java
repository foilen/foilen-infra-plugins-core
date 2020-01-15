/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.urlredirection;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.domain.Domain;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.website.Website;
import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;

/**
 * This is a url redirection to another url. <br>
 * Links to:
 * <ul>
 * <li>{Machine: (optional / many) INSTALLED_ON - Where to install that redirection.</li>
 * <li>{WebsiteCertificate: (optional / 1) USES - When using HTTPS needs one certificate.</li>
 * </ul>
 *
 * Manages:
 * <ul>
 * <li>{@link Domain}: (optional / many) MANAGES - The domains</li>
 * <li>{@link Website}: (1 per protocol) MANAGES - Website to handle the redirection</li>
 * <li>{@link UnixUser}: 1 MANAGES - The infra_url_redirection user</li>
 * </ul>
 */
public class UrlRedirection extends AbstractIPResource implements Comparable<UrlRedirection> {

    public static final String RESOURCE_TYPE = "Url Redirection";

    public static final String PROPERTY_DOMAIN_NAME = "domainName";
    public static final String PROPERTY_HTTP_REDIRECT_TO_URL = "httpRedirectToUrl";
    public static final String PROPERTY_HTTP_IS_PERMANENT = "httpIsPermanent";
    public static final String PROPERTY_HTTPS_REDIRECT_TO_URL = "httpsRedirectToUrl";
    public static final String PROPERTY_HTTPS_IS_PERMANENT = "httpsIsPermanent";

    private String domainName;

    private String httpRedirectToUrl;
    private boolean httpIsPermanent;

    private String httpsRedirectToUrl;
    private boolean httpsIsPermanent;

    public UrlRedirection() {
    }

    /**
     * Primary key.
     *
     * @param domainName
     *            the domain name
     */
    public UrlRedirection(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public int compareTo(UrlRedirection o) {
        return ComparisonChain.start() //
                .compare(this.domainName, o.domainName) //
                .result();
    }

    public String getDomainName() {
        return domainName;
    }

    public String getHttpRedirectToUrl() {
        return httpRedirectToUrl;
    }

    public String getHttpsRedirectToUrl() {
        return httpsRedirectToUrl;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.NET;
    }

    @Override
    public String getResourceDescription() {
        return "Redirection to " + Joiner.on(", ").skipNulls().join(httpRedirectToUrl, httpsRedirectToUrl);
    }

    @Override
    public String getResourceName() {
        return domainName;
    }

    public boolean isHttpIsPermanent() {
        return httpIsPermanent;
    }

    public boolean isHttpsIsPermanent() {
        return httpsIsPermanent;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void setHttpIsPermanent(boolean httpIsPermanent) {
        this.httpIsPermanent = httpIsPermanent;
    }

    public void setHttpRedirectToUrl(String httpRedirectToUrl) {
        this.httpRedirectToUrl = httpRedirectToUrl;
    }

    public void setHttpsIsPermanent(boolean httpsIsPermanent) {
        this.httpsIsPermanent = httpsIsPermanent;
    }

    public void setHttpsRedirectToUrl(String httpsRedirectToUrl) {
        this.httpsRedirectToUrl = httpsRedirectToUrl;
    }

}
