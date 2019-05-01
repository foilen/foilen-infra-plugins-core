/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mongodb;

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

public class MongoDBServerChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {
        List<ActionHandler> actions = new ArrayList<>();

        StreamTools.concat( //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), MongoDBServer.class), //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastRefreshedResources(), MongoDBServer.class), //
                ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), MongoDBServer.class).map(i -> (MongoDBServer) i.getNext()), //
                ChangesEventHandlerUtils.getFromAndToResourcesStream(changesInTransactionContext.getLastAddedLinks(), MongoDBServer.class), //
                ChangesEventHandlerUtils.getFromAndToResourcesStream(changesInTransactionContext.getLastDeletedLinks(), MongoDBServer.class) //
        ) //
                .map(it -> it.getName()) //
                .sorted().distinct() //
                .forEach(serverName -> {

                    actions.add((s, changes) -> {

                        logger.info("Processing mongodb server {}", serverName);

                        IPResourceService resourceService = services.getResourceService();
                        Optional<MongoDBServer> o = resourceService.resourceFindByPk(new MongoDBServer(serverName));
                        if (!o.isPresent()) {
                            logger.info("{} is not present. Skipping", serverName);
                            return;
                        }
                        MongoDBServer server = o.get();

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

                            applicationDefinition.setFrom("foilen/fcloud-docker-mongodb:" + server.getVersion());

                            applicationDefinition.addService("app", "/mongodb-start.sh");
                            IPApplicationDefinitionAssetsBundle assetsBundle = applicationDefinition.addAssetsBundle();
                            applicationDefinition.addContainerUserToChangeId("mongodb", unixUser.getId());

                            applicationDefinition.addPortEndpoint(27017, "MONGODB_TCP");

                            applicationDefinition.setRunAs(unixUser.getId());

                            // Data folder
                            String baseFolder = unixUser.getHomeFolder() + "/mongodb/" + serverName;
                            applicationDefinition.addVolume(new IPApplicationDefinitionVolume(baseFolder, "/var/lib/mongodb", unixUser.getId(), unixUser.getId(), "770"));

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
