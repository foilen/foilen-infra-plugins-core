/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.utils;

import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.domain.Domain;
import com.foilen.infra.resource.urlredirection.UrlRedirection;
import com.foilen.infra.resource.website.Website;

public class ActionsHandlerUtils {

    public static void addOrUpdate(IPResource resource, ChangesContext changes) {
        if (resource.getInternalId() == null) {
            changes.resourceAdd(resource);
        } else {
            changes.resourceUpdate(resource);
        }
    }

    public static Domain getOrCreateADomain(IPResourceService resourceService, String domainName) {
        Domain domain = new Domain(domainName);
        Optional<Domain> o = resourceService.resourceFindByPk(domain);
        if (o.isPresent()) {
            domain = o.get();
        }

        return domain;
    }

    public static Application getOrCreateAnApplication(IPResourceService resourceService, String applicationName) {
        Application application = new Application(applicationName);
        Optional<Application> o = resourceService.resourceFindByPk(application);
        if (o.isPresent()) {
            application = o.get();
        }

        return application;
    }

    public static UrlRedirection getOrCreateAnUrlRedirection(IPResourceService resourceService, String domainName) {
        UrlRedirection urlRedirection = new UrlRedirection(domainName);
        Optional<UrlRedirection> o = resourceService.resourceFindByPk(urlRedirection);
        if (o.isPresent()) {
            urlRedirection = o.get();
        }

        return urlRedirection;
    }

    public static Website getOrCreateAWebsite(IPResourceService resourceService, String websiteName) {
        Website website = new Website(websiteName);
        Optional<Website> o = resourceService.resourceFindByPk(website);
        if (o.isPresent()) {
            website = o.get();
        }

        return website;
    }

}
