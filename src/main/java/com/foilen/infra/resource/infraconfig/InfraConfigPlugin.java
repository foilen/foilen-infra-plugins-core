/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.infraconfig;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.google.common.base.Strings;

/**
 * This is the details of the folder structure.
 */
public class InfraConfigPlugin extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Infrastructure Plugin";

    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_SHA512 = "sha512";

    // Details
    private String url;
    private String sha512;

    public InfraConfigPlugin() {
    }

    public InfraConfigPlugin(String url, String sha512) {
        this.url = url;
        this.sha512 = sha512;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.INFRASTRUCTURE;
    }

    @Override
    public String getResourceDescription() {
        if (Strings.isNullOrEmpty(sha512)) {
            return url;
        } else {
            return url + " -> " + sha512;
        }
    }

    @Override
    public String getResourceName() {
        return url;
    }

    public String getSha512() {
        return sha512;
    }

    public String getUrl() {
        return url;
    }

    public void setSha512(String sha512) {
        this.sha512 = sha512;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
