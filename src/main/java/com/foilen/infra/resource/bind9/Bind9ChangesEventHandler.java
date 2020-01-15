/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.dns.DnsEntry;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StringTools;

public class Bind9ChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    private static final String CHANGE_TRACKER = "Bind9ChangesEventHandler-changedDnsCounts";

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        Set<String> bind9ServerNeedsRefresh = new HashSet<>();

        // Refreshes
        ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastRefreshedResources(), Bind9Server.class) //
                .forEach(bind9Server -> {
                    logger.info("Refresh requested for {}", bind9Server.getName());
                    bind9ServerNeedsRefresh.add(bind9Server.getName());
                });

        // Refreshes
        ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), Bind9Server.class) //
                .forEach(it -> {
                    Bind9Server bind9Server = (Bind9Server) it.getNext();
                    logger.info("Bind9Server updated for {}", bind9Server.getName());
                    bind9ServerNeedsRefresh.add(bind9Server.getName());
                });

        // UnixUser updated
        for (UnixUser unixUser : ChangesEventHandlerUtils.getNextResourcesOfType(changesInTransactionContext.getLastUpdatedResources(), UnixUser.class)) {
            for (Bind9Server bind9Server : services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(Bind9Server.class, LinkTypeConstants.RUN_AS, unixUser)) {
                logger.info("the attached UnixUser {} changed. Needs update {}", unixUser.getName(), bind9Server.getName());
                bind9ServerNeedsRefresh.add(bind9Server.getName());
            }
        }

        if (changesInTransactionContext.hasChangesInLastRun()) {
            logger.info("hasChangesInLastRun. Skipping for now");
        } else {
            logger.info("There were no changes in last run. Checking if any DnsEntry changed");

            // Keep track of counts in each categories. Refresh only if that changed
            String currentChangedDnsCounts = "";
            currentChangedDnsCounts += changesInTransactionContext.getAllAddedResources().stream().filter(r -> r instanceof DnsEntry).count();
            currentChangedDnsCounts += ":";
            currentChangedDnsCounts += changesInTransactionContext.getAllUpdatedResources().stream().filter(r -> r.getPrevious() instanceof DnsEntry).count();
            currentChangedDnsCounts += ":";
            currentChangedDnsCounts += changesInTransactionContext.getAllDeletedResources().stream().filter(r -> r instanceof DnsEntry).count();

            String previousChangedDnsCounts = changesInTransactionContext.getVars().get(CHANGE_TRACKER);
            if (previousChangedDnsCounts == null) {
                previousChangedDnsCounts = "0:0:0";
            }
            logger.info("previous changedDnsCounts {} ; current changedDnsCounts {}", previousChangedDnsCounts, currentChangedDnsCounts);

            if (StringTools.safeEquals(previousChangedDnsCounts, currentChangedDnsCounts)) {
                logger.info("There were no DnsEntry changed. Nothing to do");
            } else {
                changesInTransactionContext.getVars().put(CHANGE_TRACKER, currentChangedDnsCounts);

                logger.info("There were DnsEntry changed. Adding Bind9 action");
                IPResourceService resourceService = services.getResourceService();
                List<Bind9Server> bind9Servers = resourceService.resourceFindAll(resourceService.createResourceQuery(Bind9Server.class));
                for (Bind9Server bind9Server : bind9Servers) {
                    logger.info("Needs update {}", bind9Server.getName());
                    bind9ServerNeedsRefresh.add(bind9Server.getName());
                }
            }
        }

        return bind9ServerNeedsRefresh.stream() //
                .map(bind9ServerName -> new Bind9ActionHandler(bind9ServerName)) //
                .collect(Collectors.toList());

    }

}
