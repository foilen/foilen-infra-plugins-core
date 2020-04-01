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
 * This is for a MongoDB User. <br>
 * Links to:
 * <ul>
 * <li>{@link MongoDBDatabase}: (optional / many) CAN_READ - The databases that this user can read in (grants SELECT).</li>
 * <li>{@link MongoDBDatabase}: (optional / many) CAN_WRITE - The databases that this user can write in (grants INSERT, UPDATE, DELETE).</li>
 * <li>{@link MongoDBDatabase}: (optional / many) CAN_ADMIN - The databases that this user can admin (grants CREATE, ALTER, DROP).</li>
 * </ul>
 */
public class MongoDBUser extends AbstractIPResource implements Comparable<MongoDBUser> {

    public static final String RESOURCE_TYPE = "MongoDB User";

    public static final String LINK_TYPE_ADMIN = "CAN_ADMIN";
    public static final String LINK_TYPE_READ = "CAN_READ";
    public static final String LINK_TYPE_WRITE = "CAN_WRITE";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_PASSWORD = "password";

    // Basics
    private String name;
    private String description;

    // Settings
    private String password;

    public MongoDBUser() {
    }

    public MongoDBUser(String name, String description, String password) {
        this.name = name;
        this.description = description;
        this.password = password;
    }

    @Override
    public int compareTo(MongoDBUser o) {
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

    public String getPassword() {
        return password;
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

    public void setPassword(String password) {
        this.password = password;
    }

}
