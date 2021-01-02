/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.postgresql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.core.exception.ProblemException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.utils.ActionsHandlerUtils;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.StreamTools;
import com.google.common.base.Strings;

public class PostgreSqlServerChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        List<ActionHandler> actions = new ArrayList<>();

        StreamTools.concat( //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), PostgreSqlServer.class), //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastRefreshedResources(), PostgreSqlServer.class), //
                ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), PostgreSqlServer.class).map(i -> (PostgreSqlServer) i.getNext()), //
                ChangesEventHandlerUtils.getFromAndToResourcesStream(changesInTransactionContext.getLastAddedLinks(), PostgreSqlServer.class), //
                ChangesEventHandlerUtils.getFromAndToResourcesStream(changesInTransactionContext.getLastDeletedLinks(), PostgreSqlServer.class) //
        ) //
                .map(it -> it.getName()) //
                .sorted().distinct() //
                .forEach(serverName -> {

                    actions.add((s, changes) -> {

                        logger.info("Processing postgresql server {}", serverName);

                        IPResourceService resourceService = services.getResourceService();
                        Optional<PostgreSqlServer> o = resourceService.resourceFindByPk(new PostgreSqlServer(serverName));
                        if (!o.isPresent()) {
                            logger.info("{} is not present. Skipping", serverName);
                            return;
                        }
                        PostgreSqlServer server = o.get();

                        // Create a root password if none is set
                        if (Strings.isNullOrEmpty(server.getRootPassword())) {
                            server.setRootPassword(SecureRandomTools.randomHexString(25));
                            changes.resourceUpdate(server);
                        }

                        // Get the user and machines
                        List<UnixUser> unixUsers = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(server, LinkTypeConstants.RUN_AS, UnixUser.class);
                        List<Machine> machines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(server, LinkTypeConstants.INSTALLED_ON, Machine.class);

                        List<Application> desiredManageApplications = new ArrayList<>();

                        logger.debug("[{}] Running as {} on {}", serverName, unixUsers, machines);

                        if (unixUsers.size() > 1) {
                            throw new ProblemException("Cannot run as more than 1 unix user");
                        }
                        if (machines.size() > 1) {
                            throw new ProblemException("Cannot be installed on multiple machines");
                        }
                        if (unixUsers.size() == 1) {

                            UnixUser unixUser = unixUsers.get(0);

                            // Application
                            Application application = ActionsHandlerUtils.getOrCreateAnApplication(resourceService, serverName);
                            desiredManageApplications.add(application);
                            application.setDescription(server.getDescription());

                            IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
                            application.setApplicationDefinition(applicationDefinition);

                            applicationDefinition.setFrom("foilen/fcloud-docker-postgresql:" + server.getVersion());

                            applicationDefinition.addService("app", "/postgresql-start.sh");
                            IPApplicationDefinitionAssetsBundle assetsBundle = applicationDefinition.addAssetsBundle();
                            applicationDefinition.addContainerUserToChangeId("postgres", unixUser.getId());

                            applicationDefinition.addPortEndpoint(5432, "POSTGRESQL_TCP");

                            applicationDefinition.setRunAs(unixUser.getId());

                            // Auth method
                            applicationDefinition.getEnvironments().put("AUTH_METHOD", server.getAuthMethod());

                            // Data folder
                            if (unixUser.getHomeFolder() != null) {
                                String baseFolder = unixUser.getHomeFolder() + "/postgresql/" + serverName;
                                applicationDefinition.addVolume(new IPApplicationDefinitionVolume(baseFolder + "/data", "/var/lib/postgresql/data", unixUser.getId(), unixUser.getId(), "770"));
                            }

                            // Save the root password
                            String newPass = server.getRootPassword();
                            assetsBundle.addAssetContent("/newPass", newPass);

                            ActionsHandlerUtils.addOrUpdate(application, changes);

                            // Sync links
                            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.INSTALLED_ON, Machine.class, machines);
                            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.RUN_AS, UnixUser.class, unixUsers);
                        }

                        CommonResourceLink.syncToLinks(services, changes, server, LinkTypeConstants.MANAGES, Application.class, desiredManageApplications);

                    });

                });

        return actions;
    }

}
