/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.website;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerResourceStream;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StreamTools;

public class WebsiteChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    public static final String UNIX_USER_HA_PROXY_NAME = "infra_web";

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        List<ActionHandler> actions = new ArrayList<>();

        Set<String> websiteNames = new ChangesEventHandlerResourceStream<>(Website.class) //
                .linksAddFrom(changesInTransactionContext.getLastAddedLinks(), LinkTypeConstants.POINTS_TO, Application.class) //
                .linksAddFrom(changesInTransactionContext.getLastDeletedLinks(), LinkTypeConstants.POINTS_TO, Application.class) //
                .linksAddFrom(changesInTransactionContext.getLastAddedLinks(), LinkTypeConstants.USES, WebsiteCertificate.class) //
                .linksAddFrom(changesInTransactionContext.getLastDeletedLinks(), LinkTypeConstants.USES, WebsiteCertificate.class) //
                .linksAddFrom(changesInTransactionContext.getLastAddedLinks(), LinkTypeConstants.INSTALLED_ON, Machine.class) //
                .linksAddFrom(changesInTransactionContext.getLastDeletedLinks(), LinkTypeConstants.INSTALLED_ON, Machine.class) //
                .linksAddFrom(changesInTransactionContext.getLastAddedLinks(), Website.LINK_TYPE_INSTALLED_ON_NO_DNS, Machine.class) //
                .linksAddFrom(changesInTransactionContext.getLastDeletedLinks(), Website.LINK_TYPE_INSTALLED_ON_NO_DNS, Machine.class) //
                .resourcesAddOfType(changesInTransactionContext.getLastAddedResources()) //
                .resourcesAddOfType(changesInTransactionContext.getLastRefreshedResources()) //
                .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources()) //

                // For each updated UnixUser
                .resourcesAdd(new ChangesEventHandlerResourceStream<>(UnixUser.class) //
                        .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources()) //
                        .streamFromResourceClassAndLinkType(services, Website.class, LinkTypeConstants.MANAGES)) //

                // For each updated WebsiteCertificate
                .resourcesAdd(new ChangesEventHandlerResourceStream<>(WebsiteCertificate.class) //
                        .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources()) //
                        .streamFromResourceClassAndLinkType(services, Website.class, LinkTypeConstants.USES)) //

                .getResourcesStream().map(Website::getName).collect(Collectors.toCollection(() -> new HashSet<>()));

        // Manage
        websiteNames.forEach(websiteName -> {
            // For each Website, manage the UnixUser
            actions.add(new WebsiteManageUnixUsersActionHandler(websiteName));
            // For each Website, update DnsPointer
            actions.add(new WebsiteManageWebsitesActionHandler(websiteName));
        });

        // For each Machine that has Websites to it, update Application
        StreamTools.concat(websiteNames.stream() //
                .flatMap(WebsiteName -> services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(new Website(WebsiteName), LinkTypeConstants.INSTALLED_ON, Machine.class)
                        .stream()), //
                ChangesEventHandlerUtils.getToResourcesStream(changesInTransactionContext.getLastDeletedLinks(), Website.class, LinkTypeConstants.INSTALLED_ON, Machine.class), //
                websiteNames.stream() //
                        .flatMap(WebsiteName -> services.getResourceService()
                                .linkFindAllByFromResourceAndLinkTypeAndToResourceClass(new Website(WebsiteName), Website.LINK_TYPE_INSTALLED_ON_NO_DNS, Machine.class).stream()), //
                ChangesEventHandlerUtils.getToResourcesStream(changesInTransactionContext.getLastDeletedLinks(), Website.class, Website.LINK_TYPE_INSTALLED_ON_NO_DNS, Machine.class) //
        ) //
                .map(Machine::getName) //
                .sorted().distinct() //
                .forEach(machineName -> {
                    actions.add(new WebsiteManageApplicationActionHandler(machineName));
                });

        return actions;
    }

}
