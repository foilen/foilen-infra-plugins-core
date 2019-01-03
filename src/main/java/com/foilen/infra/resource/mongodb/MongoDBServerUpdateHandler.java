/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mongodb;

import java.util.List;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractFinalStateManagedResourcesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.FinalStateManagedResource;
import com.foilen.infra.plugin.v1.core.eventhandler.FinalStateManagedResourcesUpdateEventHandlerContext;
import com.foilen.infra.plugin.v1.core.exception.ProblemException;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.google.common.base.Strings;

public class MongoDBServerUpdateHandler extends AbstractFinalStateManagedResourcesEventHandler<MongoDBServer> {

    @Override
    protected void commonHandlerExecute(CommonServicesContext services, FinalStateManagedResourcesUpdateEventHandlerContext<MongoDBServer> context) {

        context.getManagedResourceTypes().add(Application.class);

        MongoDBServer mongoDBServer = context.getResource();

        String serverName = mongoDBServer.getName();
        logger.debug("[{}] Processing", serverName);

        // Create a root password if none is set
        if (Strings.isNullOrEmpty(mongoDBServer.getRootPassword())) {
            mongoDBServer.setRootPassword(SecureRandomTools.randomHexString(25));
            context.setRequestUpdateResource(true);
        }

        // Get the user and machines
        List<UnixUser> unixUsers = services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(mongoDBServer, LinkTypeConstants.RUN_AS, UnixUser.class);
        List<Machine> machines = services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(mongoDBServer, LinkTypeConstants.INSTALLED_ON, Machine.class);

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
            Application application = new Application();
            application.setName(serverName);
            application.setDescription(mongoDBServer.getDescription());

            IPApplicationDefinition applicationDefinition = application.getApplicationDefinition();

            applicationDefinition.setFrom("foilen/fcloud-docker-mongodb:" + mongoDBServer.getVersion());

            applicationDefinition.addService("app", "/mongodb-start.sh");
            IPApplicationDefinitionAssetsBundle assetsBundle = applicationDefinition.addAssetsBundle();
            applicationDefinition.addContainerUserToChangeId("mongodb", unixUser.getId());

            applicationDefinition.addPortEndpoint(27017, "MONGODB_TCP");

            applicationDefinition.setRunAs(unixUser.getId());

            // Data folder
            String baseFolder = unixUser.getHomeFolder() + "/mongodb/" + serverName;
            applicationDefinition.addVolume(new IPApplicationDefinitionVolume(baseFolder, "/var/lib/mongodb", unixUser.getId(), unixUser.getId(), "770"));

            // Save the root password
            String newPass = mongoDBServer.getRootPassword();
            assetsBundle.addAssetContent("/newPass", newPass);

            // Manage the app
            FinalStateManagedResource finalStateManagedApplication = new FinalStateManagedResource();
            finalStateManagedApplication.setManagedResource(application);
            context.getManagedResources().add(finalStateManagedApplication);

            // add Machine INSTALLED_ON to applicationDefinition (only 0 or 1)
            finalStateManagedApplication.addManagedLinksToType(LinkTypeConstants.INSTALLED_ON);
            if (machines.size() == 1) {
                Machine machine = machines.get(0);
                finalStateManagedApplication.addLinkTo(LinkTypeConstants.INSTALLED_ON, machine);
            }

            // add UnixUser RUN_AS to applicationDefinition (only 1)
            finalStateManagedApplication.addManagedLinksToType(LinkTypeConstants.RUN_AS);
            finalStateManagedApplication.addLinkTo(LinkTypeConstants.RUN_AS, unixUser);
        }
    }

    @Override
    public Class<MongoDBServer> supportedClass() {
        return MongoDBServer.class;
    }

}
