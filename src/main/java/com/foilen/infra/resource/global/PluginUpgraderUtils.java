/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.global;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.exception.ProblemException;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.smalltools.tools.SystemTools;
import com.foilen.smalltools.tools.ThreadNameStateTool;
import com.foilen.smalltools.tools.ThreadTools;
import com.foilen.smalltools.upgrader.UpgraderTools;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;

public class PluginUpgraderUtils {

    private final static Logger logger = LoggerFactory.getLogger(PluginUpgraderUtils.class);

    public static void upgrade(CommonServicesContext commonServicesContext, InternalServicesContext internalServicesContext, IPPluginDefinitionV1 ipPluginDefinitionV1,
            AbstractPluginUpgraderTask... tasksArray) {

        if ("true".equals(SystemTools.getPropertyOrEnvironment("PluginUpgrader.disable", "false"))) {
            logger.warn("PluginUpgrader.disable is true. Skipping");
            return;
        }

        String pluginName = ipPluginDefinitionV1.getPluginName();

        ThreadNameStateTool threadNameStateTool = ThreadTools.nameThread() //
                .clear() //
                .setSeparator("-") //
                .appendText("Upgrader") //
                .appendText(pluginName) //
                .change();

        try {
            logger.info("Upgrader for plugin {}", pluginName);

            List<UpgradeTask> tasks = Arrays.asList(tasksArray).stream() //
                    .map(it -> it) //
                    .map(it -> {
                        it.setCommonServicesContext(commonServicesContext);
                        it.setInternalServicesContext(internalServicesContext);
                        return it;
                    }) //
                    .collect(Collectors.toList());
            UpgraderTools upgraderTools = new UpgraderTools(tasks);

            // Get or create the UpgraderItem
            IPResourceService resourceService = commonServicesContext.getResourceService();
            UpgraderItem upgraderItem = new UpgraderItem(pluginName);
            Optional<UpgraderItem> upgraderItemO = resourceService.resourceFindByPk(upgraderItem);
            if (!upgraderItemO.isPresent()) {
                logger.info("Did not find an UpgraderItem for {} . Creating one", pluginName);
                ChangesContext changes = new ChangesContext(resourceService);
                changes.resourceAdd(upgraderItem);
                internalServicesContext.getInternalChangeService().changesExecute(changes);

                upgraderItemO = resourceService.resourceFindByPk(upgraderItem);
            }
            upgraderItem = upgraderItemO.get();

            upgraderTools.setDefaultUpgraderTracker(new UpgraderItemUpgraderTracker(resourceService, internalServicesContext.getInternalChangeService(), upgraderItem));
            upgraderTools.execute();

        } catch (Exception e) {
            throw new ProblemException("Could not execute upgrader for " + pluginName, e);
        } finally {
            threadNameStateTool.revert();
        }

    }

}
