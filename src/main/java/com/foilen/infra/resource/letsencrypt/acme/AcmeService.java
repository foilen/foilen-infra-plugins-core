/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.acme;

import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;

import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;
import com.foilen.smalltools.tuple.Tuple2;

public interface AcmeService {

    void challengeComplete(Challenge challenge);

    Tuple2<Order, Dns01Challenge> challengeDnsInit(String domainName);

    Tuple2<Order, Http01Challenge> challengeHttpInit(String domainName);

    RSACertificate requestCertificate(Order order, byte[] certificateRequest);

}