/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.infraconfig;

import java.util.ArrayList;
import java.util.List;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StreamTools;

public class InfraConfigChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {
        List<ActionHandler> actions = new ArrayList<>();

        // InfraConfig changed
        boolean changed = StreamTools.concat( //
                changesInTransactionContext.getLastAddedResources().stream().filter(it -> it instanceof InfraConfig), //
                changesInTransactionContext.getLastRefreshedResources().stream().filter(it -> it instanceof InfraConfig), //
                changesInTransactionContext.getLastDeletedResources().stream().filter(it -> it instanceof InfraConfig), //
                changesInTransactionContext.getLastUpdatedResources().stream().filter(it -> it.getNext() instanceof InfraConfig).map(it -> (InfraConfig) it.getNext()) //
        ).findAny().isPresent();
        logger.info("InfraConfig changed? {}", changed);

        // Links with InfraConfig
        if (!changed) {
            changed |= StreamTools.concat( //
                    ChangesEventHandlerUtils.getFromAndToResourcesStream(changesInTransactionContext.getLastAddedLinks(), InfraConfig.class), //
                    ChangesEventHandlerUtils.getFromAndToResourcesStream(changesInTransactionContext.getLastDeletedLinks(), InfraConfig.class) //
            ).findAny().isPresent();
            logger.info("InfraConfig links changed? {}", changed);
        }

        // InfraConfigPlugin changed (needed only if linked to InfraConfig)
        if (!changed) {
            changed |= StreamTools.concat( //
                    changesInTransactionContext.getLastAddedResources().stream().filter(it -> it instanceof InfraConfigPlugin), //
                    changesInTransactionContext.getLastDeletedResources().stream().filter(it -> it instanceof InfraConfigPlugin), //
                    changesInTransactionContext.getLastUpdatedResources().stream().filter(it -> it.getNext() instanceof InfraConfigPlugin).map(it -> (InfraConfigPlugin) it.getNext()) //
            ).findAny().isPresent();
            logger.info("InfraConfigPlugin changed? {}", changed);
        }

        // When changed, check only 1 or 0 InfraConfig
        if (changed) {
            logger.info("InfraConfig changed. Check if there is still 0 or 1 InfraConfig");

            IPResourceService resourceService = services.getResourceService();
            long countOfInfraConfig = resourceService.resourceFindAll(resourceService.createResourceQuery(InfraConfig.class)).stream().count();
            logger.info("Amount of InfraConfig: {}", countOfInfraConfig);
            if (countOfInfraConfig > 1) {
                throw new IllegalUpdateException("Cannot have more than 1 InfraConfig");
            }

            // Add action
            actions.add(new InfraConfigActionHandler());
        }

        return actions;
    }

}
