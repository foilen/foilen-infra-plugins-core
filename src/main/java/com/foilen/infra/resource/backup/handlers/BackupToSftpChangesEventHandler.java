/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.backup.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.resource.backup.resources.BackupToSftpConfig;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StreamTools;

public class BackupToSftpChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {
        List<ActionHandler> actions = new ArrayList<>();

        boolean configChanged = StreamTools.concat( //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), BackupToSftpConfig.class), //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastRefreshedResources(), BackupToSftpConfig.class), //
                ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), BackupToSftpConfig.class).map(it -> (BackupToSftpConfig) it.getNext()), //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastDeletedResources(), BackupToSftpConfig.class) //
        //
        ).count() > 0;

        logger.info("Backup config changed? {}", configChanged);
        if (configChanged) {
            // All machines
            logger.info("Updating all machines");
            services.getResourceService().resourceFindAll(services.getResourceService().createResourceQuery(Machine.class)).stream() //
                    .map(it -> new BackupToSftpActionHandler(it.getName())) //
                    .forEach(action -> actions.add(action));
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
                    .forEach(machineName -> actions.add(new BackupToSftpActionHandler(machineName)));
        }

        return actions;
    }

}
