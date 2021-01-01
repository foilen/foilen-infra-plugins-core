/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.infraconfig;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;

/**
 * This is for managing the basic infrastructure resources: Login and UI. <br>
 * Links to:
 * <ul>
 * <li>WebsiteCertificate: (optional / 1) LOGIN_USES - The certificate.</li>
 * <li>Machine: (many) LOGIN_INSTALLED_ON - The machine to install the login service on.</li>
 * <li>MariaDBServer: (1) LOGIN_USES - The database to use.</li>
 * <li>MariaDBDatabase: (1) LOGIN_USES - The database to use.</li>
 * <li>MariaDBUser: (1) LOGIN_USES - The database user to use.</li>
 * <li>UnixUser: (1) LOGIN_USES - The unix user to RUN_AS.</li>
 * <li>WebsiteCertificate: (optional / 1) UI_USES - The certificate.</li>
 * <li>Machine: (many) UI_INSTALLED_ON - The machine to install the ui service on.</li>
 * <li>MariaDBServer: (1) UI_USES - The database to use (for migration).</li>
 * <li>MariaDBDatabase: (1) UI_USES - The database to use (for migration).</li>
 * <li>MariaDBUser: (1) UI_USES - The database user to use (for migration).</li>
 * <li>MongoDBServer: (1) UI_USES - The database to use.</li>
 * <li>MongoDBDatabase: (1) UI_USES - The database to use.</li>
 * <li>MongoDBUser: (1) UI_USES - The database user to use.</li>
 * <li>UnixUser: (1) UI_USES - The unix user to RUN_AS.</li>
 * <li>{@link InfraConfigPlugin}: (optional / many) UI_USES - The plugins to install</li>
 * </ul>
 *
 * Manages:
 * <ul>
 * <li>Application: The Login application that will automatically point to the Machine}s on which it is LOGIN_INSTALLED_ON</li>
 * <li>Website: The Login website</li>
 * <li>Application: The UI application that will automatically point to the Machine}s on which it is UI_INSTALLED_ON</li>
 * <li>Website: The UI website</li>
 * </ul>
 */
public class InfraConfig extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Infrastructure Configuration";

    public static final String LINK_TYPE_LOGIN_INSTALLED_ON = "LOGIN_INSTALLED_ON";
    public static final String LINK_TYPE_LOGIN_USES = "LOGIN_USES";

    public static final String LINK_TYPE_UI_INSTALLED_ON = "UI_INSTALLED_ON";
    public static final String LINK_TYPE_UI_USES = "UI_USES";

    public static final String PROPERTY_APPLICATION_ID = "applicationId";
    public static final String PROPERTY_MAIL_HOST = "mailHost";
    public static final String PROPERTY_MAIL_PORT = "mailPort";
    public static final String PROPERTY_MAIL_USERNAME = "mailUsername";
    public static final String PROPERTY_MAIL_PASSWORD = "mailPassword";

    public static final String PROPERTY_LOGIN_DOMAIN_NAME = "loginDomainName";
    public static final String PROPERTY_LOGIN_EMAIL_FROM = "loginEmailFrom";
    public static final String PROPERTY_LOGIN_ADMINISTRATOR_EMAIL = "loginAdministratorEmail";
    public static final String PROPERTY_LOGIN_CSRF_SALT = "loginCsrfSalt";
    public static final String PROPERTY_LOGIN_COOKIE_SIGNATURE_SALT = "loginCookieSignatureSalt";
    public static final String PROPERTY_LOGIN_VERSION = "loginVersion";

    public static final String PROPERTY_UI_DOMAIN_NAME = "uiDomainName";
    public static final String PROPERTY_UI_EMAIL_FROM = "uiEmailFrom";
    public static final String PROPERTY_UI_ALERTS_TO_EMAIL = "uiAlertsToEmail";
    public static final String PROPERTY_UI_CSRF_SALT = "uiCsrfSalt";
    public static final String PROPERTY_UI_LOGIN_COOKIE_SIGNATURE_SALT = "uiLoginCookieSignatureSalt";
    public static final String PROPERTY_UI_VERSION = "uiVersion";
    public static final String PROPERTY_UI_DEBUG = "uiDebug";
    public static final String PROPERTY_UI_INFINITE_LOOP_TIMEOUT_IN_MS = "uiInfiniteLoopTimeoutInMs";

    // Common
    private String applicationId;
    private String mailHost = "127.0.0.1";
    private int mailPort = 25;
    private String mailUsername;
    private String mailPassword;

    // Login
    private String loginDomainName;
    private String loginEmailFrom;
    private String loginAdministratorEmail;
    private String loginCsrfSalt;
    private String loginCookieSignatureSalt;
    private String loginVersion;

    // UI
    private String uiDomainName;
    private String uiEmailFrom;
    private String uiAlertsToEmail;
    private String uiCsrfSalt;
    private String uiLoginCookieSignatureSalt;
    private String uiVersion;
    private boolean uiDebug;
    private Long uiInfiniteLoopTimeoutInMs = 120000L;

    public InfraConfig() {
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getLoginAdministratorEmail() {
        return loginAdministratorEmail;
    }

    public String getLoginCookieSignatureSalt() {
        return loginCookieSignatureSalt;
    }

    public String getLoginCsrfSalt() {
        return loginCsrfSalt;
    }

    public String getLoginDomainName() {
        return loginDomainName;
    }

    public String getLoginEmailFrom() {
        return loginEmailFrom;
    }

    public String getLoginVersion() {
        return loginVersion;
    }

    public String getMailHost() {
        return mailHost;
    }

    public String getMailPassword() {
        return mailPassword;
    }

    public int getMailPort() {
        return mailPort;
    }

    public String getMailUsername() {
        return mailUsername;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.INFRASTRUCTURE;
    }

    @Override
    public String getResourceDescription() {
        return "Basic Infrastructure (login and UI)";
    }

    @Override
    public String getResourceName() {
        return "Infrastructure Configuration";
    }

    public String getUiAlertsToEmail() {
        return uiAlertsToEmail;
    }

    public String getUiCsrfSalt() {
        return uiCsrfSalt;
    }

    public String getUiDomainName() {
        return uiDomainName;
    }

    public String getUiEmailFrom() {
        return uiEmailFrom;
    }

    public Long getUiInfiniteLoopTimeoutInMs() {
        return uiInfiniteLoopTimeoutInMs;
    }

    public String getUiLoginCookieSignatureSalt() {
        return uiLoginCookieSignatureSalt;
    }

    public String getUiVersion() {
        return uiVersion;
    }

    public boolean isUiDebug() {
        return uiDebug;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setLoginAdministratorEmail(String loginAdministratorEmail) {
        this.loginAdministratorEmail = loginAdministratorEmail;
    }

    public void setLoginCookieSignatureSalt(String loginCookieSignatureSalt) {
        this.loginCookieSignatureSalt = loginCookieSignatureSalt;
    }

    public void setLoginCsrfSalt(String loginCsrfSalt) {
        this.loginCsrfSalt = loginCsrfSalt;
    }

    public void setLoginDomainName(String loginDomainName) {
        this.loginDomainName = loginDomainName;
    }

    public void setLoginEmailFrom(String loginEmailFrom) {
        this.loginEmailFrom = loginEmailFrom;
    }

    public void setLoginVersion(String loginVersion) {
        this.loginVersion = loginVersion;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public void setMailPassword(String mailPassword) {
        this.mailPassword = mailPassword;
    }

    public void setMailPort(int mailPort) {
        this.mailPort = mailPort;
    }

    public void setMailUsername(String mailUsername) {
        this.mailUsername = mailUsername;
    }

    public void setUiAlertsToEmail(String uiAlertsToEmail) {
        this.uiAlertsToEmail = uiAlertsToEmail;
    }

    public void setUiCsrfSalt(String uiCsrfSalt) {
        this.uiCsrfSalt = uiCsrfSalt;
    }

    public void setUiDebug(boolean uiDebug) {
        this.uiDebug = uiDebug;
    }

    public void setUiDomainName(String uiDomainName) {
        this.uiDomainName = uiDomainName;
    }

    public void setUiEmailFrom(String uiEmailFrom) {
        this.uiEmailFrom = uiEmailFrom;
    }

    public void setUiInfiniteLoopTimeoutInMs(Long uiInfiniteLoopTimeoutInMs) {
        this.uiInfiniteLoopTimeoutInMs = uiInfiniteLoopTimeoutInMs;
    }

    public void setUiLoginCookieSignatureSalt(String uiLoginCookieSignatureSalt) {
        this.uiLoginCookieSignatureSalt = uiLoginCookieSignatureSalt;
    }

    public void setUiVersion(String uiVersion) {
        this.uiVersion = uiVersion;
    }

}
