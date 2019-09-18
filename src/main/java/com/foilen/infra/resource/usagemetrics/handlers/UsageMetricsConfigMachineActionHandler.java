/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.usagemetrics.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.email.resources.JamesEmailServer;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.mariadb.MariaDBDatabase;
import com.foilen.infra.resource.mariadb.MariaDBServer;
import com.foilen.infra.resource.mariadb.MariaDBUser;
import com.foilen.infra.resource.unixuser.SystemUnixUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.usagemetrics.model.DatabaseInfo;
import com.foilen.infra.resource.usagemetrics.resources.UsageMetricsConfig;
import com.foilen.smalltools.hash.HashSha256;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;

public class UsageMetricsConfigMachineActionHandler extends AbstractBasics implements ActionHandler {

    private String machineName;

    public UsageMetricsConfigMachineActionHandler(String machineName) {
        this.machineName = machineName;
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing machine {}", machineName);

        IPResourceService resourceService = services.getResourceService();
        Optional<Machine> o = resourceService.resourceFindByPk(new Machine(machineName));
        if (!o.isPresent()) {
            logger.info("{} is not present. Skipping", machineName);
            return;
        }
        Machine machine = o.get();

        // Get the configuration
        Optional<UsageMetricsConfig> optionalUsageMetricsConfig = resourceService.resourceFind(resourceService.createResourceQuery(UsageMetricsConfig.class));
        if (!optionalUsageMetricsConfig.isPresent()) {
            logger.info("Config is not present. Skipping");
            return;
        }
        UsageMetricsConfig config = optionalUsageMetricsConfig.get();

        // Get root user
        Optional<SystemUnixUser> rootUnixUserOptional = resourceService.resourceFind(resourceService.createResourceQuery(SystemUnixUser.class).propertyEquals(UnixUser.PROPERTY_ID, 0L));
        SystemUnixUser rootUnixUser = null;
        if (rootUnixUserOptional.isPresent()) {
            rootUnixUser = rootUnixUserOptional.get();
        } else {
            rootUnixUser = new SystemUnixUser(0L, "root");
            changes.resourceAdd(rootUnixUser);
        }
        // Get the unix user if present
        Optional<SystemUnixUser> optionalUnixUser = resourceService.resourceFindByPk(new SystemUnixUser(0L, "root"));
        if (!optionalUnixUser.isPresent()) {
            logger.info("Unix user is not present. Skipping");
            return;
        }

        // Application
        Application agentApplication = new Application();
        agentApplication.setName("usage_agent-" + machineName.replaceAll("\\.", "_"));
        agentApplication.setDescription("Usage Agent for " + machineName);
        agentApplication.getMeta().put("usagemetrics", "true");

        IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
        agentApplication.setApplicationDefinition(applicationDefinition);

        applicationDefinition.setFrom("foilen/usage-metrics-agent:" + config.getVersion());

        applicationDefinition.setCommand("/app/bin/usage-metrics-agent /config.json");

        // Add James Servers databases on this machine
        List<DatabaseInfo> jamesDatabases = new ArrayList<>();
        AtomicInteger nextPortRedirect = new AtomicInteger(9000);
        resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(config, LinkTypeConstants.USES, JamesEmailServer.class).forEach(jamesEmailServer -> {
            logger.info("[James] Got {}", jamesEmailServer.getName());

            Optional<MariaDBUser> mariaDbUserOptional = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(jamesEmailServer, LinkTypeConstants.USES, MariaDBUser.class).stream()
                    .findFirst();
            if (mariaDbUserOptional.isEmpty()) {
                logger.info("[James] Does not have a Maria DB User set. Skipping");
                return;
            }
            MariaDBUser mariaDbUser = mariaDbUserOptional.get();
            logger.info("[James-MariaDB User] Got {} / {}", jamesEmailServer.getName(), mariaDbUser.getName());

            resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(jamesEmailServer, LinkTypeConstants.USES, MariaDBDatabase.class).forEach(mariaDbDatabase -> {
                logger.info("[James-MariaDB Database] Got {} / {}", jamesEmailServer.getName(), mariaDbDatabase.getName());

                resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(mariaDbDatabase, LinkTypeConstants.INSTALLED_ON, MariaDBServer.class).forEach(mariaDbServer -> {
                    logger.info("[James-MariaDB Database-MariaDB Server] Got {} / {} / {}", jamesEmailServer.getName(), mariaDbDatabase.getName(), mariaDbServer.getName());

                    resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(mariaDbServer, LinkTypeConstants.INSTALLED_ON, Machine.class).forEach(mariaDbMachine -> {
                        logger.info("[James-MariaDB Database-MariaDB Server-Machine] Got {} / {} / {} / {}", jamesEmailServer.getName(), mariaDbDatabase.getName(), mariaDbServer.getName(),
                                mariaDbMachine.getName());

                        if (StringTools.safeEquals(machine.getName(), mariaDbMachine.getName())) {
                            int port = nextPortRedirect.getAndIncrement();
                            jamesDatabases.add(new DatabaseInfo("127.0.0.1", port, mariaDbDatabase.getName(), mariaDbUser.getName(), mariaDbUser.getPassword()));
                            applicationDefinition.addPortRedirect(port, mariaDbMachine.getName(), mariaDbServer.getName(), DockerContainerEndpoints.MYSQL_TCP);
                        } else {
                            logger.info("Not this machine. Skip");
                        }

                    });
                });
            });
        });

        Map<String, Object> configFileContent = new HashMap<>();
        configFileContent.put("centralUri", "http://127.0.0.1:8080");
        configFileContent.put("hostname", machineName);
        configFileContent.put("hostnameKey", HashSha256.hashString(config.getHostKeySalt() + machineName));
        configFileContent.put("diskSpaceRootFs", "/hostfs/");
        configFileContent.put("jamesDatabases", jamesDatabases);
        applicationDefinition.addAssetContent("/config.json", JsonTools.prettyPrintWithoutNulls(configFileContent));

        applicationDefinition.addVolume(new IPApplicationDefinitionVolume("/", "/hostfs"));
        applicationDefinition.addVolume(new IPApplicationDefinitionVolume("/usr/bin/docker", "/usr/bin/docker"));
        applicationDefinition.addVolume(new IPApplicationDefinitionVolume("/usr/lib/x86_64-linux-gnu/libltdl.so.7.3.1", "/usr/lib/x86_64-linux-gnu/libltdl.so.7"));
        applicationDefinition.addVolume(new IPApplicationDefinitionVolume("/var/run/docker.sock", "/var/run/docker.sock"));

        // Add Central
        List<Machine> centralMachines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(config, LinkTypeConstants.INSTALLED_ON, Machine.class);
        if (centralMachines.isEmpty()) {
            logger.info("Central Machine is not present. Skipping");
            return;
        }
        if (centralMachines.size() > 1) {
            logger.info("Too many Central Machines");
            throw new IllegalUpdateException("Must have a singe Central Machine. Got " + centralMachines.size());
        }
        applicationDefinition.addPortRedirect(8080, centralMachines.get(0).getName(), UsageMetricsConfigCentralActionHandler.CENTRAL_APPLICATION_NAME, DockerContainerEndpoints.HTTP_TCP);

        // Create or update
        Optional<Application> existingApplicationOptional = resourceService.resourceFindByPk(agentApplication);
        if (existingApplicationOptional.isPresent()) {
            Application existingApplication = existingApplicationOptional.get();
            changes.resourceUpdate(existingApplication, agentApplication);
        } else {
            changes.resourceAdd(agentApplication);
        }

        // Apply links
        changes.linkAdd(agentApplication, LinkTypeConstants.RUN_AS, optionalUnixUser.get());
        changes.linkAdd(agentApplication, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(config, LinkTypeConstants.MANAGES, agentApplication);
        changes.linkAdd(machine, LinkTypeConstants.MANAGES, agentApplication);

    }

}
