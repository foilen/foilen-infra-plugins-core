/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.plugin;

import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;

public interface LetsencryptHelper {

    static final String LAST_FAILURE = "lastFailure";

    void checkUrlOrFail(String url);

    /**
     * Get the ACME configuration, create the challenges and start the timer to complete.
     *
     * @param services
     *            all services
     * @param changes
     *            the changes to make
     * @param certificatesToUpdate
     *            the certificates to generate challenges for
     */
    void createChallengesAndCreateTimer(CommonServicesContext services, ChangesContext changes, List<WebsiteCertificate> certificatesToUpdate);

}