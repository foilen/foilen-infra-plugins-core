/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.acme.mock;

import org.shredzone.acme4j.challenge.Dns01Challenge;

public class Dns01ChallengeMock extends Dns01Challenge {

    private static final long serialVersionUID = 1L;

    public Dns01ChallengeMock() {
        super(null, null);
    }

    @Override
    public String getAuthorization() {
        throw new IllegalStateException("Mock not implemented");
    }

    @Override
    public String getDigest() {
        throw new IllegalStateException("Mock not implemented");
    }

}
