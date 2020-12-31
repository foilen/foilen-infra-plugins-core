/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.cronjob;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.google.common.collect.ComparisonChain;

/**
 * This is for any cron job that is installed on a machine. <br>
 * Links to:
 * <ul>
 * <li>{@link UnixUser}: (optional / 1) RUN_AS - The user that executes that application. Will update the "runAs" of the Application itself and the "runAs" of all the services that are "null"</li>
 * <li>{@link Application}: (optional / 1) USES - The application to run the service inside</li>
 * <li>{@link Machine}: (optional / many) INSTALLED_ON - The machines where to install that cron job (must be one where the application is installed on</li>
 * </ul>
 */
public class CronJob extends AbstractIPResource implements Comparable<CronJob> {

    public static final String RESOURCE_TYPE = "CronJob";

    public static final String PROPERTY_UID = "uid";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_TIME = "time";
    public static final String PROPERTY_COMMAND = "command";
    public static final String PROPERTY_WORKING_DIRECTORY = "workingDirectory";

    // Details
    private String uid = SecureRandomTools.randomBase64String(10);
    private String description;

    private String time;
    private String command;
    private String workingDirectory = null;

    public CronJob() {
    }

    /**
     * Primary key.
     *
     * @param uid
     *            the uid
     */
    public CronJob(String uid) {
        this.uid = uid;
    }

    @Override
    public int compareTo(CronJob o) {
        return ComparisonChain.start() //
                .compare(this.uid, o.uid) //
                .result();
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.INFRASTRUCTURE;
    }

    @Override
    public String getResourceDescription() {
        return description;
    }

    @Override
    public String getResourceName() {
        return uid;
    }

    public String getTime() {
        return time;
    }

    public String getUid() {
        return uid;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

}
