/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.usagemetrics.handlers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.mongodb.MongoDBServer;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.usagemetrics.resources.UsageMetricsConfig;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;

public class UsageMetricsConfigCentralActionHandler extends AbstractBasics implements ActionHandler {

    public static final String CENTRAL_APPLICATION_NAME = "usage_central";

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing central");

        IPResourceService resourceService = services.getResourceService();

        // Get the configuration
        Optional<UsageMetricsConfig> optionalUsageMetricsConfig = resourceService.resourceFind(resourceService.createResourceQuery(UsageMetricsConfig.class));
        if (!optionalUsageMetricsConfig.isPresent()) {
            logger.info("Config is not present. Skipping");
            return;
        }

        // Get the unix user if present
        Optional<UnixUser> optionalUnixUser = resourceService.resourceFind(resourceService.createResourceQuery(UnixUser.class) //
                .propertyEquals(UnixUser.PROPERTY_NAME, UsageMetricsConfigChangesEventHandler.UNIX_USER));
        if (!optionalUnixUser.isPresent()) {
            logger.info("Unix user is not present. Skipping");
            return;
        }

        // Get the links
        UsageMetricsConfig config = optionalUsageMetricsConfig.get();
        List<MongoDBServer> mongoDbServers = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(config, LinkTypeConstants.USES, MongoDBServer.class);
        if (mongoDbServers.isEmpty()) {
            logger.info("MongoDB Server is not present. Skipping");
            return;
        }
        if (mongoDbServers.size() > 1) {
            logger.info("Too many MongoDB Servers");
            throw new IllegalUpdateException("Must have a singe MongoDB Server. Got " + mongoDbServers.size());
        }
        List<Machine> centralMachines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(config, LinkTypeConstants.INSTALLED_ON, Machine.class);
        if (centralMachines.isEmpty()) {
            logger.info("Central Machine is not present. Skipping");
            return;
        }
        if (centralMachines.size() > 1) {
            logger.info("Too many Central Machines");
            throw new IllegalUpdateException("Must have a singe Central Machine. Got " + centralMachines.size());
        }
        List<Website> websites = resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(Website.class, LinkTypeConstants.POINTS_TO, config);

        // Application
        Application centralApplication = new Application();
        centralApplication.setName(CENTRAL_APPLICATION_NAME);
        centralApplication.setDescription("Central");

        IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
        centralApplication.setApplicationDefinition(applicationDefinition);

        applicationDefinition.setFrom("foilen/usage-metrics-central:" + config.getVersion());

        applicationDefinition.setCommand("/app/bin/usage-metrics-central /config.json");
        Map<String, String> configFileContent = new HashMap<>();
        configFileContent.put("hostKeySalt", config.getHostKeySalt());
        configFileContent.put("mongoUri", "mongodb://" + config.getMongoUser() + ":" + config.getMongoPassword() + "@127.0.0.1:27017/" + config.getMongoDatabase() + "?authSource=admin");
        applicationDefinition.addAssetContent("/config.json", JsonTools.prettyPrintWithoutNulls(configFileContent));

        applicationDefinition.addPortEndpoint(8080, DockerContainerEndpoints.HTTP_TCP);

        // Add mongodb
        MongoDBServer mongoDbServer = mongoDbServers.get(0);
        List<Machine> mongoDbmachines = services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(mongoDbServer, LinkTypeConstants.INSTALLED_ON, Machine.class);
        if (mongoDbmachines.isEmpty()) {
            return;
        }
        Machine mongoDbmachine = mongoDbmachines.get(0);
        applicationDefinition.addPortRedirect(27017, mongoDbmachine.getName(), mongoDbServer.getName(), DockerContainerEndpoints.MONGODB_TCP);

        // Create or update
        Optional<Application> existingApplicationOptional = resourceService.resourceFindByPk(centralApplication);
        if (existingApplicationOptional.isPresent()) {
            Application existingApplication = existingApplicationOptional.get();
            changes.resourceUpdate(existingApplication, centralApplication);
        } else {
            changes.resourceAdd(centralApplication);
        }

        // Apply links
        changes.linkAdd(config, LinkTypeConstants.MANAGES, centralApplication);
        CommonResourceLink.syncFromLinks(services, changes, Website.class, LinkTypeConstants.POINTS_TO, centralApplication, websites);
        CommonResourceLink.syncToLinks(services, changes, centralApplication, LinkTypeConstants.INSTALLED_ON, Machine.class, centralMachines);
        CommonResourceLink.syncToLinks(services, changes, centralApplication, LinkTypeConstants.RUN_AS, UnixUser.class, Arrays.asList(optionalUnixUser.get()));

    }

}
