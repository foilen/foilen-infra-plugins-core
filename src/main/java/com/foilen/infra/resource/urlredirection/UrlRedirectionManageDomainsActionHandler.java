/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.urlredirection;

import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.domain.DomainResourceHelper;
import com.foilen.smalltools.tools.AbstractBasics;

public class UrlRedirectionManageDomainsActionHandler extends AbstractBasics implements ActionHandler {

    private String urlRedirectionDomainName;

    public UrlRedirectionManageDomainsActionHandler(String urlRedirectionDomainName) {
        this.urlRedirectionDomainName = urlRedirectionDomainName;
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing {}", urlRedirectionDomainName);

        IPResourceService resourceService = services.getResourceService();
        Optional<UrlRedirection> o = resourceService.resourceFindByPk(new UrlRedirection(urlRedirectionDomainName));
        if (!o.isPresent()) {
            logger.info("{} is not present. Skipping", urlRedirectionDomainName);
            return;
        }
        UrlRedirection urlRedirection = o.get();

        DomainResourceHelper.syncManagedLinks(services, changes, urlRedirection, urlRedirection.getDomainName());

    }

}
