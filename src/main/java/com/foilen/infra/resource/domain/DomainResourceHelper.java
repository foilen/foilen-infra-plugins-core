/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.validator.routines.DomainValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.utils.ActionsHandlerUtils;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DomainTools;
import com.foilen.smalltools.tools.StringTools;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class DomainResourceHelper extends AbstractBasics {

    private static final Logger logger = LoggerFactory.getLogger(DomainResourceHelper.class);

    public static Optional<Domain> getParent(Domain domain) {
        return getParent(domain.getName());
    }

    /**
     * Get the parent Domain.
     *
     * @param childDomainName
     *            the name of the domain to check for a parent
     * @return the parent or nothing if it is already the root domain
     */
    public static Optional<Domain> getParent(String childDomainName) {
        List<String> parts = DomainTools.getPartsAsList(childDomainName);

        // Last non-empty value
        if (parts.size() <= 1) {
            return Optional.empty();
        }

        // Remove one part
        parts.remove(0);

        // Validate is a domain
        String parentDomainName = Joiner.on('.').join(parts);
        if (DomainValidator.getInstance().isValid(parentDomainName)) {
            return Optional.of(new Domain(parentDomainName));
        } else {
            return Optional.empty();
        }
    }

    public static void syncManagedLinks(CommonServicesContext servicesContext, ChangesContext changes, IPResource fromResource, Collection<String> domainNames) {

        // Get or create new domain
        List<Domain> domains = domainNames.stream() //
                .filter(domainName -> !Strings.isNullOrEmpty(domainName)) //
                .sorted() //
                .map(domainName -> ActionsHandlerUtils.getOrCreateADomain(servicesContext.getResourceService(), domainName)) //
                .collect(Collectors.toList());
        // Create the domains that are not in the system
        domains.forEach(domain -> {
            if (domain.getInternalId() == null) {
                boolean alreadyAdded = changes.getResourcesToAdd().stream() //
                        .anyMatch(r -> r instanceof Domain //
                                && StringTools.safeEquals(domain.getName(), ((Domain) r).getName()));
                if (alreadyAdded) {
                    logger.info("[CHANGES] {} : Already present in the added changes", domain);
                } else {
                    logger.info("[CHANGES] {} : Add", domain);
                    changes.resourceAdd(domain);
                }
            } else {
                logger.info("[CHANGES] {} : Already present in the system", domain);
            }
        });

        // Sync
        CommonResourceLink.syncToLinks(servicesContext, changes, fromResource, LinkTypeConstants.MANAGES, Domain.class, domains);

    }

    public static void syncManagedLinks(CommonServicesContext servicesContext, ChangesContext changes, IPResource fromResource, String... domainNames) {
        syncManagedLinks(servicesContext, changes, fromResource, Arrays.asList(domainNames));
    }

}
