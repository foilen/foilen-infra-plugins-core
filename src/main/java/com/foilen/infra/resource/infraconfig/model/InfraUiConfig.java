/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.infraconfig.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.foilen.infra.plugin.v1.model.infra.InfraLoginConfigDetails;

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InfraUiConfig {

    // UI
    private String baseUrl;
    private Long infiniteLoopTimeoutInMs;

    // MySql
    private String mysqlHostName;
    private int mysqlPort = 3306;
    private String mysqlDatabaseName;
    private String mysqlDatabaseUserName;
    private String mysqlDatabasePassword;

    // Mongo
    private String mongoUri;

    // Email server
    private String mailHost;
    private int mailPort;
    private String mailUsername; // Optional
    private String mailPassword; // Optional

    // Email the Infra sends
    private String mailFrom;
    private String mailAlertsTo;

    // Login
    private InfraLoginConfigDetails loginConfigDetails = new InfraLoginConfigDetails();
    private String loginCookieSignatureSalt;

    // Security
    private String csrfSalt;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getCsrfSalt() {
        return csrfSalt;
    }

    public Long getInfiniteLoopTimeoutInMs() {
        return infiniteLoopTimeoutInMs;
    }

    public InfraLoginConfigDetails getLoginConfigDetails() {
        return loginConfigDetails;
    }

    public String getLoginCookieSignatureSalt() {
        return loginCookieSignatureSalt;
    }

    public String getMailAlertsTo() {
        return mailAlertsTo;
    }

    public String getMailFrom() {
        return mailFrom;
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

    public String getMongoUri() {
        return mongoUri;
    }

    public String getMysqlDatabaseName() {
        return mysqlDatabaseName;
    }

    public String getMysqlDatabasePassword() {
        return mysqlDatabasePassword;
    }

    public String getMysqlDatabaseUserName() {
        return mysqlDatabaseUserName;
    }

    public String getMysqlHostName() {
        return mysqlHostName;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setCsrfSalt(String csrfSalt) {
        this.csrfSalt = csrfSalt;
    }

    public void setInfiniteLoopTimeoutInMs(Long infiniteLoopTimeoutInMs) {
        this.infiniteLoopTimeoutInMs = infiniteLoopTimeoutInMs;
    }

    public void setLoginConfigDetails(InfraLoginConfigDetails loginConfigDetails) {
        this.loginConfigDetails = loginConfigDetails;
    }

    public void setLoginCookieSignatureSalt(String loginCookieSignatureSalt) {
        this.loginCookieSignatureSalt = loginCookieSignatureSalt;
    }

    public void setMailAlertsTo(String mailAlertsTo) {
        this.mailAlertsTo = mailAlertsTo;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
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

    public void setMongoUri(String mongoUri) {
        this.mongoUri = mongoUri;
    }

    public void setMysqlDatabaseName(String mysqlDatabaseName) {
        this.mysqlDatabaseName = mysqlDatabaseName;
    }

    public void setMysqlDatabasePassword(String mysqlDatabasePassword) {
        this.mysqlDatabasePassword = mysqlDatabasePassword;
    }

    public void setMysqlDatabaseUserName(String mysqlDatabaseUserName) {
        this.mysqlDatabaseUserName = mysqlDatabaseUserName;
    }

    public void setMysqlHostName(String mysqlHostName) {
        this.mysqlHostName = mysqlHostName;
    }

    public void setMysqlPort(int mysqlPort) {
        this.mysqlPort = mysqlPort;
    }

}
