/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.cronjob;

import java.util.ArrayList;
import java.util.List;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerResourceStream;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.base.Joiner;

public class CronJobChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        List<ActionHandler> actions = new ArrayList<>();

        logger.info("Search for modified CronJob");

        // Check mounted volumes host path belong to the unix users
        ChangesEventHandlerResourceStream<CronJob> allChangedCronJobs = new ChangesEventHandlerResourceStream<>(CronJob.class);
        allChangedCronJobs.resourcesAddOfType(changesInTransactionContext.getLastAddedResources());
        allChangedCronJobs.resourcesAddOfType(changesInTransactionContext.getLastRefreshedResources());
        allChangedCronJobs.resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        allChangedCronJobs.linksAddFromAndTo(changesInTransactionContext.getLastAddedLinks());
        allChangedCronJobs.linksAddFromAndTo(changesInTransactionContext.getLastDeletedLinks());

        // Check working directories and commands doesn't have non-path characters
        boolean wrong = allChangedCronJobs.getResourcesStream().anyMatch(cronJob -> {
            boolean wrongCommandOrWorkingDirectory = !CommonValidation.validPath(cronJob.getCommand());
            wrongCommandOrWorkingDirectory |= !CommonValidation.validPath(cronJob.getWorkingDirectory());
            return wrongCommandOrWorkingDirectory;
        });
        if (wrong) {
            throw new IllegalUpdateException("All commands and working directories cannot have non-path characters");
        }

        List<String> wrongs = new ArrayList<>();
        allChangedCronJobs.getResourcesStream().forEach(cronJob -> {
            if (!CommonValidation.validPath(cronJob.getCommand())) {
                wrongs.add("Cron Job " + cronJob.getUid() + " Command");
            }
            if (!CommonValidation.validPath(cronJob.getWorkingDirectory())) {
                wrongs.add("Cron Job " + cronJob.getUid() + " Working Directory");
            }
        });
        if (!wrongs.isEmpty()) {
            throw new IllegalUpdateException("All commands and working directories cannot have non-path characters: " + Joiner.on(",").join(wrongs));
        }

        return actions;
    }

}
