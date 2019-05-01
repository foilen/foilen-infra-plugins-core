/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.backup.handlers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.application.model.ExecutionPolicy;
import com.foilen.infra.resource.backup.resources.BackupToGoogleCloudConfig;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.SystemUnixUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CharsetTools;
import com.foilen.smalltools.tools.JsonTools;

public class BackupToGoogleCloudActionHandler extends AbstractBasics implements ActionHandler {

    private String machineName;

    public BackupToGoogleCloudActionHandler(String machineName) {
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
        Optional<BackupToGoogleCloudConfig> optionalBackupToGoogleCloudConfig = resourceService.resourceFind(resourceService.createResourceQuery(BackupToGoogleCloudConfig.class));

        // Application
        Application backupApplication = new Application();
        backupApplication.setDescription("Backup to Google Cloud for " + machineName);
        String applicationName = "backup_google-" + machineName.replaceAll("\\.", "_");
        backupApplication.setName(applicationName);

        Optional<Application> existingApplicationOptional = resourceService.resourceFindByPk(backupApplication);
        if (optionalBackupToGoogleCloudConfig.isPresent()) {
            logger.info("[{}] There is a configuration", machineName);

            BackupToGoogleCloudConfig config = optionalBackupToGoogleCloudConfig.get();

            IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
            backupApplication.setApplicationDefinition(applicationDefinition);

            applicationDefinition.setFrom("foilen/fcloud-docker-backup-to-google-cloud:1.0.1");

            backupApplication.setExecutionPolicy(ExecutionPolicy.CRON);
            backupApplication.setExecutionCronDetails("22 0 * * *");
            applicationDefinition.setCommand("BUCKET=" + config.getBucketName() + " HOST_NAME=" + machineName + " /backup.sh");

            Map<String, String> googlecloudKey = new HashMap<>();
            googlecloudKey.put("type", "service_account");
            googlecloudKey.put("auth_uri", "https://accounts.google.com/o/oauth2/auth");
            googlecloudKey.put("token_uri", "https://accounts.google.com/o/oauth2/token");
            googlecloudKey.put("auth_provider_x509_cert_url", "https://www.googleapis.com/oauth2/v1/certs");
            googlecloudKey.put("project_id", config.getProjectId());
            googlecloudKey.put("private_key_id", config.getPrivateKeyId());
            googlecloudKey.put("private_key", config.getPrivateKey());
            googlecloudKey.put("client_id", config.getClientId());
            googlecloudKey.put("client_email", config.getClientEmail());
            try {
                googlecloudKey.put("client_x509_cert_url", URLEncoder.encode("https://www.googleapis.com/robot/v1/metadata/x509/" + config.getClientEmail(), CharsetTools.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalUpdateException("Problem setting the client " + e.getMessage());
            }
            applicationDefinition.addAssetContent("/google-cloud-key.json", JsonTools.prettyPrint(googlecloudKey));

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
