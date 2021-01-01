/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.handlers.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
public class EmailConfigDomainAndRelay {

    private String domain;
    private String hostname;
    private int port;
    private String username;
    private String password;

    public String getDomain() {
        return domain;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public EmailConfigDomainAndRelay setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public EmailConfigDomainAndRelay setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public EmailConfigDomainAndRelay setPassword(String password) {
        this.password = password;
        return this;
    }

    public EmailConfigDomainAndRelay setPort(int port) {
        this.port = port;
        return this;
    }

    public EmailConfigDomainAndRelay setUsername(String username) {
        this.username = username;
        return this;
    }
}
