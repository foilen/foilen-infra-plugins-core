/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.backup.handlers;

import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.application.model.ExecutionPolicy;
import com.foilen.infra.resource.backup.resources.BackupToSftpConfig;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.SystemUnixUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tools.AbstractBasics;

public class BackupToSftpActionHandler extends AbstractBasics implements ActionHandler {

    private String machineName;

    public BackupToSftpActionHandler(String machineName) {
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
        Optional<BackupToSftpConfig> optionalBackupToSftpConfig = resourceService.resourceFind(resourceService.createResourceQuery(BackupToSftpConfig.class));

        // Application
        Application backupApplication = new Application();
        backupApplication.setDescription("Backup to SFTP for " + machineName);
        String applicationName = "backup_sftp-" + machineName.replaceAll("\\.", "_");
        backupApplication.setName(applicationName);

        Optional<Application> existingApplicationOptional = resourceService.resourceFindByPk(backupApplication);
        if (optionalBackupToSftpConfig.isPresent()) {
            logger.info("[{}] There is a configuration", machineName);

            BackupToSftpConfig config = optionalBackupToSftpConfig.get();

            IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
            backupApplication.setApplicationDefinition(applicationDefinition);

            applicationDefinition.setFrom("foilen/fcloud-docker-backup-to-sftp:1.0.0");

            backupApplication.setExecutionPolicy(ExecutionPolicy.CRON);
            backupApplication.setExecutionCronDetails(config.getTime());
            StringBuilder command = new StringBuilder();
            command.append("SSH_HOSTNAME=").append(config.getSshHostname()).append(" ");
            command.append("SSH_PORT=").append(config.getSshPort()).append(" ");
            command.append("SSH_USER=").append(config.getSshUser()).append(" ");
            command.append("REMOTE_PATH=").append(config.getRemotePath()).append(" ");
            command.append("HOST_NAME=").append(machineName).append(" ");
            applicationDefinition.setCommand(command.toString() + "/backup.sh");

            applicationDefinition.addAssetContent("/id_rsa", config.getSshPrivateKey());

            applicationDefinition.addVolume(new IPApplicationDefinitionVolume("/home", "/backupRoot"));
            applicationDefinition.setRunAs(0L);

            // Get or create root user
            Optional<SystemUnixUser> rootUnixUserOptional = resourceService.resourceFind(resourceService.createResourceQuery(SystemUnixUser.class).propertyEquals(UnixUser.PROPERTY_ID, 0L));
            SystemUnixUser rootUnixUser = null;
            if (rootUnixUserOptional.isPresent()) {
                rootUnixUser = rootUnixUserOptional.get();
            } else {
                rootUnixUser = new SystemUnixUser(0L, "root");
                changes.resourceAdd(rootUnixUser);
            }

            // Create or update
            if (existingApplicationOptional.isPresent()) {
                Application existingApplication = existingApplicationOptional.get();
                changes.resourceUpdate(existingApplication, backupApplication);
            } else {
                changes.resourceAdd(backupApplication);
                changes.linkAdd(backupApplication, LinkTypeConstants.RUN_AS, rootUnixUser);
            }

            // Apply links
            changes.linkAdd(backupApplication, LinkTypeConstants.INSTALLED_ON, machine);
            changes.linkAdd(machine, LinkTypeConstants.MANAGES, backupApplication);

        } else {
            logger.info("[{}] No configuration to install", machineName);
            if (existingApplicationOptional.isPresent()) {
                logger.info("[{}] Deleting existing Backup script", machineName);
                changes.resourceDelete(existingApplicationOptional.get());
            }
        }

    }

}
