/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.backup.handlers;

import java.util.List;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.application.model.ExecutionPolicy;
import com.foilen.infra.resource.backup.resources.BackupToSftpConfig;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.SystemUnixUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tuple.Tuple3;

public class MachineBackupToSftpEventHandler extends AbstractUpdateEventHandler<Machine> {

    @Override
    public void addHandler(CommonServicesContext services, ChangesContext changes, Machine resource) {
        commonHandlerExecute(services, changes, resource);
    }

    @Override
    public void checkAndFix(CommonServicesContext services, ChangesContext changes, Machine resource) {
        commonHandlerExecute(services, changes, resource);
    }

    protected void commonHandlerExecute(CommonServicesContext services, ChangesContext changes, Machine machine) {

        IPResourceService resourceService = services.getResourceService();

        // Get the configuration
        Optional<BackupToSftpConfig> optionalBackupToSftpConfig = resourceService.resourceFind(resourceService.createResourceQuery(BackupToSftpConfig.class));

        // Application
        String machineName = machine.getName();
        Application backupApplication = new Application();
        backupApplication.setDescription("Backup to SFTP for " + machineName);
        backupApplication.setName("backup_sftp-" + machineName.replaceAll("\\.", "_"));

        Optional<Application> existingApplicationOptional = resourceService.resourceFindByPk(backupApplication);
        if (optionalBackupToSftpConfig.isPresent()) {
            logger.info("[{}] There is a configuration", machineName);

            BackupToSftpConfig config = optionalBackupToSftpConfig.get();

            IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
            backupApplication.setApplicationDefinition(applicationDefinition);

            applicationDefinition.setFrom("foilen/fcloud-docker-backup-to-sftp:1.0.0");

            backupApplication.setExecutionPolicy(ExecutionPolicy.CRON);
            backupApplication.setExecutionCronDetails("22 0 * * *");
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
                if (updateResourceIfDifferent(backupApplication, existingApplication)) {
                    changes.resourceUpdate(existingApplication, backupApplication);
                }
                backupApplication = existingApplication;
            } else {
                changes.resourceAdd(backupApplication);
                changes.linkAdd(backupApplication, LinkTypeConstants.RUN_AS, rootUnixUser);
            }

            // Apply links
            changes.linkAdd(backupApplication, LinkTypeConstants.INSTALLED_ON, machine);

        } else {
            logger.info("[{}] No configuration to install", machineName);
            if (existingApplicationOptional.isPresent()) {
                logger.info("[{}] Deleting existing Backup script", machineName);
                changes.resourceDelete(existingApplicationOptional.get());
            }
        }
    }

    @Override
    public void deleteHandler(CommonServicesContext services, ChangesContext changes, Machine resource, List<Tuple3<IPResource, String, IPResource>> previousLinks) {
        removeApplicationFor(services, changes, resource.getName());
    }

    private void removeApplicationFor(CommonServicesContext services, ChangesContext changes, String machineName) {
        IPResourceService resourceService = services.getResourceService();

        // Application
        Application backupApplication = new Application();
        backupApplication.setDescription("Backup to SFTP for " + machineName);
        backupApplication.setName("backup_sftp-" + machineName.replaceAll("\\.", "_"));

        Optional<Application> existingApplicationOptional = resourceService.resourceFindByPk(backupApplication);
        if (existingApplicationOptional.isPresent()) {
            Application existingApplication = existingApplicationOptional.get();
            logger.info("Removing application {}", existingApplication.getName());
            changes.resourceDelete(existingApplication);
        }
    }

    @Override
    public Class<Machine> supportedClass() {
        return Machine.class;
    }

    @Override
    public void updateHandler(CommonServicesContext services, ChangesContext changes, Machine previousResource, Machine newResource) {
        if (!StringTools.safeEquals(previousResource.getName(), newResource.getName())) {
            removeApplicationFor(services, changes, previousResource.getName());
        }
        commonHandlerExecute(services, changes, newResource);
    }

}
