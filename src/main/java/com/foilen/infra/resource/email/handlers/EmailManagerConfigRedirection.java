/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.handlers;

import java.util.ArrayList;
import java.util.List;

import com.foilen.smalltools.tools.AbstractBasics;

public class EmailManagerConfigRedirection extends AbstractBasics implements Comparable<EmailManagerConfigRedirection> {

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

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRedirectTos(List<String> redirectTos) {
        this.redirectTos = redirectTos;
    }

}
