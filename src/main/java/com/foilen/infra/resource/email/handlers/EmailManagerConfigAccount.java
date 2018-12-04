/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.handlers;

import com.foilen.smalltools.tools.AbstractBasics;

public class EmailManagerConfigAccount extends AbstractBasics implements Comparable<EmailManagerConfigAccount> {

    private String email;
    private String passwordSha512;

    @Override
    public int compareTo(EmailManagerConfigAccount o) {
        return this.email.compareTo(o.email);
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordSha512() {
        return passwordSha512;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordSha512(String passwordSha512) {
        this.passwordSha512 = passwordSha512;
    }

}
