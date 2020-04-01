/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mongodb;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.google.common.collect.ComparisonChain;

/**
 * This is for a MongoDB Database. <br>
 * Links to:
 * <ul>
 * <li>{@link MongoDBServer}: (optional / many) INSTALLED_ON - On which server to install it.</li>
 * </ul>
 */
public class MongoDBDatabase extends AbstractIPResource implements Comparable<MongoDBDatabase> {

    public static final String RESOURCE_TYPE = "MongoDB Database";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DESCRIPTION = "description";

    // Basics
    private String name;
    private String description;

    public MongoDBDatabase() {
    }

    public MongoDBDatabase(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public int compareTo(MongoDBDatabase o) {
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

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

}
