/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.usagemetrics.handlers;

import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.unixuser.SystemUnixUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.unixuser.helper.UnixUserAvailableIdHelper;
import com.foilen.infra.resource.usagemetrics.resources.UsageMetricsConfig;
import com.foilen.smalltools.tools.AbstractBasics;

public class UsageMetricsManageUnixUsersActionHandler extends AbstractBasics implements ActionHandler {

    private void createCentralUser(ChangesContext changes, IPResourceService resourceService, UsageMetricsConfig usageMetricsConfig) {
        // Get the unix user if present
        logger.info("Check unix user exists: {}", UsageMetricsConfigChangesEventHandler.UNIX_USER);
        Optional<UnixUser> optionalUnixUser = resourceService.resourceFind(resourceService.createResourceQuery(UnixUser.class) //
                .propertyEquals(UnixUser.PROPERTY_NAME, UsageMetricsConfigChangesEventHandler.UNIX_USER));
        if (optionalUnixUser.isPresent()) {
            logger.info("Unix user already exist. Skipping");
            return;
        }

        // Create it
        logger.info("Create unix user: {}", UsageMetricsConfigChangesEventHandler.UNIX_USER);
        UnixUser unixUser = new UnixUser();
        unixUser.setId(UnixUserAvailableIdHelper.getNextAvailableId());
        unixUser.setName(UsageMetricsConfigChangesEventHandler.UNIX_USER);
        changes.resourceAdd(unixUser);
        changes.linkAdd(usageMetricsConfig, LinkTypeConstants.MANAGES, unixUser);
    }

    private void createRootUser(ChangesContext changes, IPResourceService resourceService) {
        // Get the unix user if present
        String rootName = "root";
        logger.info("Check unix user exists: {}", rootName);
        Optional<SystemUnixUser> optionalUnixUser = resourceService.resourceFindByPk(new SystemUnixUser(0L, rootName));
        if (optionalUnixUser.isPresent()) {
            logger.info("Unix user already exist. Skipping");
            return;
        }

        // Create it
        logger.info("Create unix user: {}", rootName);
        SystemUnixUser unixUser = new SystemUnixUser(0L, rootName);
        changes.resourceAdd(unixUser);
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        IPResourceService resourceService = services.getResourceService();

        // Get the configuration
        Optional<UsageMetricsConfig> optionalUsageMetricsConfig = resourceService.resourceFind(resourceService.createResourceQuery(UsageMetricsConfig.class));
        if (!optionalUsageMetricsConfig.isPresent()) {
            logger.info("Config is not present. Skipping");
            return;
        }

        createCentralUser(changes, resourceService, optionalUsageMetricsConfig.get());
        createRootUser(changes, resourceService);

    }

}
