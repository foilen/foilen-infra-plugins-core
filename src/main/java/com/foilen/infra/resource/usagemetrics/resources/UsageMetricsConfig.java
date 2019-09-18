/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.usagemetrics.resources;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.email.resources.JamesEmailServer;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.mongodb.MongoDBServer;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.tools.SecureRandomTools;

/**
 * This is the Usage Metrics configuration.<br/>
 *
 * Links to:
 * <ul>
 * <li>{@link MongoDBServer}: (1) USES - The Mongo DB to use</li>
 * <li>{@link Machine}: (1) INSTALLED_ON - Where to install the central</li>
 * <li>{@link JamesEmailServer}: (optional / many) USES - The James Email Servers to check for space usage</li>
 * </ul>
 * 
 * Links from:
 * <ul>
 * <li>{@link Website}: (optional / many) POINTS_TO - Websites that will point to them</li>
 * </ul>
 *
 * Manages:
 * <ul>
 * <li>{@link Application}: The central app</li>
 * <li>{@link Application}: The agent apps</li>
 * </ul>
 */
public class UsageMetricsConfig extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Usage Metrics Config";

    public static final String PROPERTY_UID = "uid";

    public static final String PROPERTY_VERSION = "version";

    public static final String PROPERTY_HOST_KEY_SALT = "hostKeySalt";
    public static final String PROPERTY_MONGO_DATABASE = "mongoDatabase";
    public static final String PROPERTY_MONGO_USER = "mongoUser";
    public static final String PROPERTY_MONGO_PASSWORD = "mongoPassword";

    // Details
    private String uid = SecureRandomTools.randomBase64String(10);

    private String version;

    private String hostKeySalt;
    private String mongoDatabase;
    private String mongoUser;
    private String mongoPassword;

    public UsageMetricsConfig() {
    }

    /**
     * Primary key.
     *
     * @param uid
     *            the uid
     */
    public UsageMetricsConfig(String uid) {
        this.uid = uid;
    }

    public String getHostKeySalt() {
        return hostKeySalt;
    }

    public String getMongoDatabase() {
        return mongoDatabase;
    }

    public String getMongoPassword() {
        return mongoPassword;
    }

    public String getMongoUser() {
        return mongoUser;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.INFRASTRUCTURE;
    }

    @Override
    public String getResourceDescription() {
        return uid;
    }

    @Override
    public String getResourceName() {
        return uid;
    }

    public String getUid() {
        return uid;
    }

    public String getVersion() {
        return version;
    }

    public UsageMetricsConfig setHostKeySalt(String hostKeySalt) {
        this.hostKeySalt = hostKeySalt;
        return this;
    }

    public UsageMetricsConfig setMongoDatabase(String mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
        return this;
    }

    public UsageMetricsConfig setMongoPassword(String mongoPassword) {
        this.mongoPassword = mongoPassword;
        return this;
    }

    public UsageMetricsConfig setMongoUser(String mongoUser) {
        this.mongoUser = mongoUser;
        return this;
    }

    public UsageMetricsConfig setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public UsageMetricsConfig setVersion(String version) {
        this.version = version;
        return this;
    }

}
