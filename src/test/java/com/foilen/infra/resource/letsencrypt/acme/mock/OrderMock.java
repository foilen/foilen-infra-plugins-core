/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.acme.mock;

import java.net.URL;
import java.security.KeyPair;

import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;

import com.foilen.infra.resource.letsencrypt.acme.LetsencryptException;

public class OrderMock extends Order {

    private static final long serialVersionUID = 1L;

    public static Login login;
    private static URL location;

    static {
        try {
            location = new URL("https://example.com");
            KeyPair keyPair = new KeyPair(null, null);
            Session session = new Session("https://example.com");
            login = new Login(location, keyPair, session);
        } catch (Exception e) {
            throw new LetsencryptException("Problem setting the mock", e);
        }
    }

    public OrderMock() {
        super(login, location);
    }

}
