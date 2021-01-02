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
 * This is for a MariaDB User. <br>
 * Links to:
 * <ul>
 * <li>{@link MariaDBDatabase}: (optional / many) CAN_READ - The databases that this user can read in (grants SELECT).</li>
 * <li>{@link MariaDBDatabase}: (optional / many) CAN_WRITE - The databases that this user can write in (grants INSERT, UPDATE, DELETE).</li>
 * <li>{@link MariaDBDatabase}: (optional / many) CAN_ADMIN - The databases that this user can admin (grants CREATE, ALTER, DROP).</li>
 * </ul>
 */
public class MariaDBUser extends AbstractIPResource implements Comparable<MariaDBUser> {

    public static final String RESOURCE_TYPE = "MariaDB User";

    public static final String LINK_TYPE_ADMIN = "CAN_ADMIN";
    public static final String LINK_TYPE_READ = "CAN_READ";
    public static final String LINK_TYPE_WRITE = "CAN_WRITE";

    public static final String PROPERTY_UID = "uid";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_PASSWORD = "password";

    // Basics
    private String uid = SecureRandomTools.randomBase64String(10);
    private String name;
    private String description;

    // Settings
    private String password = SecureRandomTools.randomHexString(25);

    public MariaDBUser() {
    }

    public MariaDBUser(String name) {
        this.name = name;
    }

    public MariaDBUser(String name, String description, String password) {
        this.name = name;
        this.description = description;
        this.password = password;
    }

    @Override
    public int compareTo(MariaDBUser o) {
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

    public String getUid() {
        return uid;
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

    public void setUid(String uid) {
        this.uid = uid;
    }

}
