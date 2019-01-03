/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.acme;

public class LetsencryptException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LetsencryptException(String message) {
        super(message);
    }

    public LetsencryptException(String message, Throwable cause) {
        super(message, cause);
    }

}
