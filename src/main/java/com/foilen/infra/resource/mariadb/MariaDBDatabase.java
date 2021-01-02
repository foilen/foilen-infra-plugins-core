/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mariadb;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.google.common.collect.ComparisonChain;

/**
 * This is for a MariaDB Database. <br>
 * Links to:
 * <ul>
 * <li>{@link MariaDBServer}: (optional / many) INSTALLED_ON - On which server to install it.</li>
 * </ul>
 */
public class MariaDBDatabase extends AbstractIPResource implements Comparable<MariaDBDatabase> {

    public static final String RESOURCE_TYPE = "MariaDB Database";

    public static final String PROPERTY_UID = "uid";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DESCRIPTION = "description";

    // Basics
    private String uid = SecureRandomTools.randomBase64String(10);
    private String name;
    private String description;

    public MariaDBDatabase() {
    }

    public MariaDBDatabase(String name) {
        this.name = name;
    }

    public MariaDBDatabase(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public int compareTo(MariaDBDatabase o) {
        return ComparisonChain.start() //
                .compare(this.name, o.name) //
                .result();
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.DATABASE;
    }

    @Override
    public String getResourceDescription() {
        return description;
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
