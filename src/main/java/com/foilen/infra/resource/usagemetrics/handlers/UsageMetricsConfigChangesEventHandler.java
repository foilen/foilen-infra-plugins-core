/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.usagemetrics.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerResourceStream;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.usagemetrics.resources.UsageMetricsConfig;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StreamTools;
import com.foilen.smalltools.tools.StringTools;

public class UsageMetricsConfigChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    public static final String UNIX_USER = "infra_usage";

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {
        List<ActionHandler> actions = new ArrayList<>();

        // Check configuration changed
        boolean configChanged = new ChangesEventHandlerResourceStream<>(UsageMetricsConfig.class) //
                .resourcesAddOfType(changesInTransactionContext.getLastAddedResources()) //
                .resourcesAddOfType(changesInTransactionContext.getLastRefreshedResources()) //
                .resourcesAddOfType(changesInTransactionContext.getLastDeletedResources()) //
                .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources()) //
                .linksAddFromAndTo(changesInTransactionContext.getLastAddedLinks()) //
                .linksAddFromAndTo(changesInTransactionContext.getLastDeletedLinks()) //
                .getResourcesStream().count() > 0;

        logger.info("Usage Metrics config changed? {}", configChanged);
        if (configChanged) {

            // Unix user
            logger.info("Updating user");
            actions.add(new UsageMetricsManageUnixUsersActionHandler());

            // All machines
            logger.info("Updating all machines");
            services.getResourceService().resourceFindAll(services.getResourceService().createResourceQuery(Machine.class)).stream() //
                    .map(it -> new UsageMetricsConfigMachineActionHandler(it.getName())) //
                    .forEach(action -> actions.add(action));

            // Central
            logger.info("Updating central");
            actions.add(new UsageMetricsConfigCentralActionHandler());

        } else {
            // Only new machines
            logger.info("Updating only changed machines");
            StreamTools.concat( //
                    ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), Machine.class), //
                    ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), Machine.class)
                            .flatMap(it -> Arrays.asList((Machine) it.getPrevious(), (Machine) it.getNext()).stream()) //
            ) //
                    .map(it -> it.getName()) //
                    .sorted().distinct() //
                    .forEach(machineName -> actions.add(new UsageMetricsConfigMachineActionHandler(machineName)));
        }

        // Removed MANAGE links
        new ChangesEventHandlerResourceStream<>(Application.class) //
                .linksAddTo(changesInTransactionContext.getLastDeletedLinks(), new String[] { LinkTypeConstants.MANAGES }) //
                .getResourcesStream() //
                .filter(application -> StringTools.safeEquals("true", application.getMeta().get("usagemetrics"))) //
                .forEach(application -> {
                    // Delete if no new Manage link
                    List<? extends IPResource> managedBy = services.getResourceService().linkFindAllByLinkTypeAndToResource(LinkTypeConstants.MANAGES, application);

                    boolean keep = managedBy.stream().anyMatch(r -> r instanceof UsageMetricsConfig);
                    keep &= managedBy.stream().anyMatch(r -> r instanceof Machine);

                    if (!keep) {
                        actions.add((s, c) -> c.resourceDelete(application));
                    }
                });

        ;

        return actions;
    }

}
