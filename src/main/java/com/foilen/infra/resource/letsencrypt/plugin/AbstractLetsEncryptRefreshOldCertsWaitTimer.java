/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.plugin;

import com.foilen.infra.plugin.v1.core.eventhandler.TimerEventHandler;
import com.foilen.infra.resource.letsencrypt.acme.AcmeService;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.ResourceTools;

public abstract class AbstractLetsEncryptRefreshOldCertsWaitTimer extends AbstractBasics implements TimerEventHandler {

    protected static final String CA_CERTIFICATE_TEXT = ResourceTools.getResourceAsString("/com/foilen/infra/resource/letsencrypt/lets-encrypt-r3-cross-signed.pem");

    protected AcmeService acmeService;
    protected LetsencryptHelper letsencryptHelper;

    public AbstractLetsEncryptRefreshOldCertsWaitTimer(AcmeService acmeService, LetsencryptHelper letsencryptHelper) {
        this.acmeService = acmeService;
        this.letsencryptHelper = letsencryptHelper;
    }

}
