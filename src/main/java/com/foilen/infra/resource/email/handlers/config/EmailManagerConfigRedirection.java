/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.handlers.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
public class EmailManagerConfigRedirection implements Comparable<EmailManagerConfigRedirection> {

    private String email;
    private List<String> redirectTos = new ArrayList<>();

    @Override
    public int compareTo(EmailManagerConfigRedirection o) {
        return this.email.compareTo(o.email);
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRedirectTos() {
        return redirectTos;
    }

    public EmailManagerConfigRedirection setEmail(String email) {
        this.email = email;
        return this;
    }

    public EmailManagerConfigRedirection setRedirectTos(List<String> redirectTos) {
        this.redirectTos = redirectTos;
        return this;
    }

}
