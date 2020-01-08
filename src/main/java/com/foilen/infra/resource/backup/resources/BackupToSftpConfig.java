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
public class BackupToSftpConfig extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Backup To SFTP Config";

    public static final String PROPERTY_UID = "uid";

    public static final String PROPERTY_SSH_HOSTNAME = "sshHostname";
    public static final String PROPERTY_SSH_PORT = "sshPort";
    public static final String PROPERTY_SSH_USER = "sshUser";
    public static final String PROPERTY_SSH_PRIVATE_KEY = "sshPrivateKey";
    public static final String PROPERTY_REMOTE_PATH = "remotePath";

    public static final String PROPERTY_TIME = "time";

    // Details
    private String uid = SecureRandomTools.randomBase64String(10);

    private String sshHostname;
    private int sshPort = 22;

    private String sshUser;
    private String sshPrivateKey;

    private String remotePath;

    private String time = "22 0 * * *";

    public String getRemotePath() {
        return remotePath;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.INFRASTRUCTURE;
    }

    @Override
    public String getResourceDescription() {
        return sshHostname + " | " + sshUser + " | " + remotePath;
    }

    @Override
    public String getResourceName() {
        return uid;
    }

    public String getSshHostname() {
        return sshHostname;
    }

    public int getSshPort() {
        return sshPort;
    }

    public String getSshPrivateKey() {
        return sshPrivateKey;
    }

    public String getSshUser() {
        return sshUser;
    }

    public String getTime() {
        return time;
    }

    public String getUid() {
        return uid;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public void setSshHostname(String sshHostname) {
        this.sshHostname = sshHostname;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public void setSshPrivateKey(String sshPrivateKey) {
        this.sshPrivateKey = sshPrivateKey;
    }

    public void setSshUser(String sshUser) {
        this.sshUser = sshUser;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
