/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.backup.resources;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.application.Application;
import com.foilen.smalltools.tools.SecureRandomTools;

/**
 * This is the backup configuration.<br/>
 * Manages:
 * <ul>
 * <li>{@link Application}: The script that backups daily</li>
 * </ul>
 */
public class BackupToGoogleCloudConfig extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Backup To Google Cloud Config";

    public static final String PROPERTY_UID = "uid";

    public static final String PROPERTY_PROJECT_ID = "projectId";
    public static final String PROPERTY_BUCKET_NAME = "bucketName";
    public static final String PROPERTY_PRIVATE_KEY_ID = "privateKeyId";
    public static final String PROPERTY_PRIVATE_KEY = "privateKey";
    public static final String PROPERTY_CLIENT_ID = "clientId";
    public static final String PROPERTY_CLIENT_EMAIL = "clientEmail";

    // Details
    private String uid = SecureRandomTools.randomBase64String(10);

    private String projectId;
    private String bucketName;

    private String privateKeyId;
    private String privateKey;

    private String clientId;
    private String clientEmail;

    public String getBucketName() {
        return bucketName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public String getClientId() {
        return clientId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPrivateKeyId() {
        return privateKeyId;
    }

    public String getProjectId() {
        return projectId;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.INFRASTRUCTURE;
    }

    @Override
    public String getResourceDescription() {
        return clientEmail + " | " + projectId + " | " + bucketName;
    }

    @Override
    public String getResourceName() {
        return uid;
    }

    public String getUid() {
        return uid;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setPrivateKeyId(String privateKeyId) {
        this.privateKeyId = privateKeyId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
