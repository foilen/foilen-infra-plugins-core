/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.website;

import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.domain.DomainResourceHelper;
import com.foilen.smalltools.tools.AbstractBasics;

public class WebsiteManageDomainsActionHandler extends AbstractBasics implements ActionHandler {

    private String websiteName;

    public WebsiteManageDomainsActionHandler(String websiteName) {
        this.websiteName = websiteName;
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing {}", websiteName);

        IPResourceService resourceService = services.getResourceService();

        Optional<Website> o = resourceService.resourceFindByPk(new Website(websiteName));
        if (!o.isPresent()) {
            logger.info("{} is not present. Skipping", websiteName);
            return;
        }
        Website website = o.get();

        DomainResourceHelper.syncManagedLinks(services, changes, website, website.getDomainNames());
    }

}
