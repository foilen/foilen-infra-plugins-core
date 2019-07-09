/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.website.Website;

/**
 * This is an Apache server that supports PHP. {@link Website}s can point to this to automatically point to the create application.<br/>
 * Links to:
 * <ul>
 * <li>{@link UnixUser}: (1) RUN_AS - The user that executes the website</li>
 * <li>{@link Machine}: (optional / many) INSTALLED_ON - The machines where to install that service</li>
 * <li>{@link AttachablePart}: (optional / many) ATTACHED - The parts to attach</li>
 * <li>{@link ApachePhpHtPasswd}: (optional / many) USES - The details for basic auth users</li>
 * <li>{@link ApachePhpFolder}: (optional / many) USES - The folders details</li>
 * </ul>
 *
 * Manages:
 * <ul>
 * <li>{@link Application}: The apache application</li>
 * </ul>
 *
 * Links from:
 * <ul>
 * <li>{@link Website}: (optional / many) POINTS_TO - Websites that will point to them</li>
 * </ul>
 */
public class ApachePhp extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Apache and PHP";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_BASE_PATH = "basePath";
    public static final String PROPERTY_MAIN_SITE_RELATIVE_PATH = "mainSiteRelativePath";
    public static final String PROPERTY_VERSION = "version";
    @Deprecated
    public static final String PROPERTY_EMAIL_SENDER_MSMTP = "emailSenderMsmtp"; // TODO 2019-07-08 Remove later
    public static final String PROPERTY_EMAIL_SENDER = "emailSender";
    public static final String PROPERTY_DEFAULT_EMAIL_FROM = "defaultEmailFrom";

    // Details
    private String name;
    private String basePath;
    private String mainSiteRelativePath = "/";
    private boolean emailSenderMsmtp = true; // TODO 2019-07-08 Remove later
    private EmailSender emailSender = EmailSender.SENDMAIL_TO_MSMTP;
    private String defaultEmailFrom;

    // Settings
    private String version = "5.5.9-1";

    public ApachePhp() {
    }

    /**
     * Primary key.
     *
     * @param name
     *            the name
     */
    public ApachePhp(String name) {
        this.name = name;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getDefaultEmailFrom() {
        return defaultEmailFrom;
    }

    public EmailSender getEmailSender() {
        return emailSender;
    }

    public String getMainSiteRelativePath() {
        return mainSiteRelativePath;
    }

    public String getName() {
        return name;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.NET;
    }

    @Override
    public String getResourceDescription() {
        return "Apache and PHP application";
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Deprecated
    public boolean isEmailSenderMsmtp() {
        return emailSenderMsmtp;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setDefaultEmailFrom(String defaultEmailFrom) {
        this.defaultEmailFrom = defaultEmailFrom;
    }

    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Deprecated
    public void setEmailSenderMsmtp(boolean emailSenderMsmtp) {
        this.emailSenderMsmtp = emailSenderMsmtp;
    }

    public void setMainSiteRelativePath(String mainSiteRelativePath) {
        this.mainSiteRelativePath = mainSiteRelativePath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
