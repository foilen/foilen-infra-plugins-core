/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.smalltools.tools.SecureRandomTools;

/**
 * This is the details of the folder structure.
 */
public class ApachePhpFolder extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Apache and PHP - Folder";

    public static final String PROPERTY_UID = "uid";
    public static final String PROPERTY_BASE_PATH = "basePath";
    public static final String PROPERTY_RELATIVE_PATH = "relativePath";
    public static final String PROPERTY_ALIAS = "alias";

    // Details
    private String uid = SecureRandomTools.randomBase64String(10);
    private String basePath;
    private String relativePath = "/";
    private String alias;

    public ApachePhpFolder() {
    }

    public ApachePhpFolder(String basePath, String relativePath, String alias) {
        this.basePath = basePath;
        this.relativePath = relativePath;
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.NET;
    }

    @Override
    public String getResourceDescription() {
        return basePath + " : " + relativePath + " -> " + alias;
    }

    @Override
    public String getResourceName() {
        return uid;
    }

    public String getUid() {
        return uid;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
