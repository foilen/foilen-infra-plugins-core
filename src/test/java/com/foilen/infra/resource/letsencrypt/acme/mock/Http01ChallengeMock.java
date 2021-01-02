/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.acme.mock;

import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.toolbox.JSON;

import com.foilen.infra.resource.letsencrypt.acme.LetsencryptException;
import com.foilen.smalltools.tools.ResourceTools;

public class Http01ChallengeMock extends Http01Challenge {

    private static final long serialVersionUID = 1L;

    private static JSON data;

    static {
        try {
            data = JSON.parse(ResourceTools.getResourceAsString("Http01ChallengeMock-order.json", Http01ChallengeMock.class));
        } catch (Exception e) {
            throw new LetsencryptException("Problem setting the mock", e);
        }
    }

    public Http01ChallengeMock() {
        super(OrderMock.login, data);
    }

    @Override
    public String getAuthorization() {
        return "_auth_";
    }

    @Override
    public String getToken() {
        return "_tok_";
    }

}
