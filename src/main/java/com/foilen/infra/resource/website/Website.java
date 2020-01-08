/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.website;

import java.util.SortedSet;
import java.util.TreeSet;

import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;

/**
 * This is a website that points to the application that is serving it. <br>
 * Links to:
 * <ul>
 * <li>Application: (required / many) POINTS_TO - The applications that are serving that web site. (are the end points ; not where the web site is installed)</li>
 * <li>WebsiteCertificate: (optional / 1) USES - When using HTTPS needs one certificate.</li>
 * <li>Machine: (optional / many) INSTALLED_ON - The machines where to install that web site</li>
 * <li>Machine: (optional / many) INSTALLED_ON_NO_DNS - The machines where to install that application, but won't have a DnsPointer on them</li>
 * </ul>
 *
 * Manages:
 * <ul>
 * <li>DnsPointer: (optional / many) POINTS_TO - Some domain names that will automatically point to the Machines on which it is INSTALLED_ON</li>
 * <li>{UnixUser: 1 MANAGES - The infra_web user</li>
 * </ul>
 */
public class Website extends AbstractIPResource implements Comparable<Website> {

    public static final String RESOURCE_TYPE = "Website";

    public static final String LINK_TYPE_INSTALLED_ON_NO_DNS = "INSTALLED_ON_NO_DNS";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DOMAIN_NAMES = "domainNames";
    public static final String PROPERTY_IS_HTTPS = "https";
    public static final String PROPERTY_IS_HTTPS_ORIGIN_TO_HTTP = "httpsOriginToHttp";
    public static final String PROPERTY_APPLICATION_ENDPOINT = "applicationEndpoint";

    // Network
    private String name;
    private SortedSet<String> domainNames = new TreeSet<>();

    private boolean isHttps;
    private boolean isHttpsOriginToHttp;
    private String applicationEndpoint = DockerContainerEndpoints.HTTP_TCP; // Default: HTTP_TCP

    public Website() {
    }

    /**
     * Primary key.
     *
     * @param name
     *            the name
     */
    public Website(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Website o) {
        return ComparisonChain.start() //
                .compare(this.name, o.name) //
                .result();
    }

    public String getApplicationEndpoint() {
        return applicationEndpoint;
    }

    public SortedSet<String> getDomainNames() {
        return domainNames;
    }

    public String getName() {
        return name;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.NET;
    }

    @Override
    public String getResourceDescription() {
        return Joiner.on(", ").join(domainNames);
    }

    @Override
    public String getResourceName() {
        return getName();
    }

    public boolean isHttps() {
        return isHttps;
    }

    public boolean isHttpsOriginToHttp() {
        return isHttpsOriginToHttp;
    }

    public void setApplicationEndpoint(String applicationEndpoint) {
        this.applicationEndpoint = applicationEndpoint;
    }

    public void setDomainNames(SortedSet<String> domainNames) {
        this.domainNames = domainNames;
    }

    public void setHttps(boolean isHttps) {
        this.isHttps = isHttps;
    }

    public void setHttpsOriginToHttp(boolean isHttpsOriginToHttp) {
        this.isHttpsOriginToHttp = isHttpsOriginToHttp;
    }

    public void setName(String name) {
        this.name = name;
    }

}
