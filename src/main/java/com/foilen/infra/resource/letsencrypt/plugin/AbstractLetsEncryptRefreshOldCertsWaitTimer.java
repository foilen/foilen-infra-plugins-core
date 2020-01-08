/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.plugin;

import com.foilen.infra.plugin.v1.core.eventhandler.TimerEventHandler;
import com.foilen.infra.resource.letsencrypt.acme.AcmeService;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.ResourceTools;

public abstract class AbstractLetsEncryptRefreshOldCertsWaitTimer extends AbstractBasics implements TimerEventHandler {

    protected static final String CA_CERTIFICATE_TEXT = ResourceTools.getResourceAsString("/com/foilen/infra/resource/letsencrypt/lets-encrypt-x3-cross-signed.pem");

    protected AcmeService acmeService;
    protected LetsencryptHelper letsencryptHelper;

    public AbstractLetsEncryptRefreshOldCertsWaitTimer(AcmeService acmeService, LetsencryptHelper letsencryptHelper) {
        this.acmeService = acmeService;
        this.letsencryptHelper = letsencryptHelper;
    }

    protected String getAllMessages(Throwable e) {

        StringBuilder messages = new StringBuilder();

        boolean first = true;
        while (e != null) {
            if (first) {
                first = true;
            } else {
                messages.append(" ; ");
            }
            if (e.getMessage() != null) {
                messages.append(messages);
            }

            e = e.getCause();
        }

        return messages.toString();
    }

}
