/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.acme.mock;

import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;

import com.foilen.infra.resource.letsencrypt.acme.AcmeService;
import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tuple.Tuple2;

public class AcmeServiceMock extends AbstractBasics implements AcmeService {

    @Override
    public void challengeComplete(Challenge challenge) {
    }

    @Override
    public Tuple2<Order, Dns01Challenge> challengeDnsInit(String domainName) {
        return new Tuple2<>(new OrderMock(), new Dns01ChallengeMock());
    }

    @Override
    public Tuple2<Order, Http01Challenge> challengeHttpInit(String domainName) {
        return new Tuple2<>(new OrderMock(), new Http01ChallengeMock());
    }

    @Override
    public RSACertificate requestCertificate(Order order, byte[] certificateRequest) {
        throw new IllegalStateException("Mock not implemented");
    }

}
