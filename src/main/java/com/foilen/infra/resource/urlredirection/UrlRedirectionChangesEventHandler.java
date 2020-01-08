/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.urlredirection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StreamTools;

public class UrlRedirectionChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    public static final String UNIX_USER_REDIRECTION_NAME = "infra_url_redirection";

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        List<ActionHandler> actions = new ArrayList<>();

        // Manage
        Set<String> urlRedirectionNames = StreamTools.concat(//
                ChangesEventHandlerUtils.getFromResourcesStream(changesInTransactionContext.getLastAddedLinks(), UrlRedirection.class, LinkTypeConstants.USES, WebsiteCertificate.class), //
                ChangesEventHandlerUtils.getFromResourcesStream(changesInTransactionContext.getLastDeletedLinks(), UrlRedirection.class, LinkTypeConstants.USES, WebsiteCertificate.class), //
                ChangesEventHandlerUtils.getFromResourcesStream(changesInTransactionContext.getLastAddedLinks(), UrlRedirection.class, LinkTypeConstants.INSTALLED_ON, Machine.class), //
                ChangesEventHandlerUtils.getFromResourcesStream(changesInTransactionContext.getLastDeletedLinks(), UrlRedirection.class, LinkTypeConstants.INSTALLED_ON, Machine.class), //
                ChangesEventHandlerUtils.getFromResourcesStream(changesInTransactionContext.getLastAddedLinks(), UrlRedirection.class, LinkTypeConstants.MANAGES, UnixUser.class), //
                ChangesEventHandlerUtils.getFromResourcesStream(changesInTransactionContext.getLastDeletedLinks(), UrlRedirection.class, LinkTypeConstants.MANAGES, UnixUser.class), //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), UrlRedirection.class), //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastRefreshedResources(), UrlRedirection.class), //
                ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), UrlRedirection.class).map(i -> (UrlRedirection) i.getNext()))//
                .map(UrlRedirection::getDomainName) //
                .collect(Collectors.toCollection(() -> new HashSet<>())); //

        urlRedirectionNames.forEach(urlRedirectionDomainName -> {
            // For each UrlRedirection, manage the UnixUser
            actions.add(new UrlRedirectionManageUnixUsersActionHandler(urlRedirectionDomainName));
            // For each UrlRedirection, update Website
            actions.add(new UrlRedirectionManageWebsitesActionHandler(urlRedirectionDomainName));
        });

        // For each updated UnixUser
        for (UnixUser unixUser : ChangesEventHandlerUtils.getNextResourcesOfType(changesInTransactionContext.getLastUpdatedResources(), UnixUser.class)) {
            for (UrlRedirection urlRedirection : services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(UrlRedirection.class, LinkTypeConstants.MANAGES, unixUser)) {
                logger.info("the managed UnixUser {} changed. Needs update {}", unixUser.getName(), urlRedirection.getDomainName());
                urlRedirectionNames.add(urlRedirection.getDomainName());
            }
        }

        // For each Machine that has UrlRedirections to it, update Application
        StreamTools.concat(urlRedirectionNames.stream() //
                .flatMap(urlRedirectionName -> services.getResourceService()
                        .linkFindAllByFromResourceAndLinkTypeAndToResourceClass(new UrlRedirection(urlRedirectionName), LinkTypeConstants.INSTALLED_ON, Machine.class).stream()), //
                ChangesEventHandlerUtils.getToResourcesStream(changesInTransactionContext.getLastDeletedLinks(), UrlRedirection.class, LinkTypeConstants.INSTALLED_ON, Machine.class) //
        ) //
                .map(Machine::getName) //
                .sorted().distinct() //
                .forEach(machineName -> {
                    actions.add(new UrlRedirectionManageApplicationActionHandler(machineName));
                });

        return actions;
    }

}
