/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mongodb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.foilen.databasetools.connection.JdbcUriConfigConnection;
import com.foilen.databasetools.manage.mongodb.MongodbManagerConfig;
import com.foilen.databasetools.manage.mongodb.MongodbManagerConfigCollectionPrivilege;
import com.foilen.databasetools.manage.mongodb.MongodbManagerConfigUser;
import com.foilen.databasetools.manage.mongodb.MongodbManagerConfigUserAndRoles;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerResourceStream;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
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
import com.foilen.smalltools.tools.CollectionsTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.google.common.base.Strings;

public class MongoDBChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    private static final String ROLE_NAME_WRITER = "writer";

    private static final String ROLE_NAME_READER = "reader";

    private static final String ROLE_NAME_ADMIN = "admin";

    private static final List<String> blacklistedDatabases = Collections.unmodifiableList(Arrays.asList( //
            "admin", //
            "config", //
            "local" //
    ));

    private static final List<String> blacklistedUsers = Collections.unmodifiableList(Arrays.asList( //
            "root" //
    ));
    private static final List<String> actionsForAdmin = Collections.unmodifiableList(Arrays.asList( //
            "enableProfiler", //
            "killAnyCursor", //
            "storageDetails", //
            "compact", //
            "validate" //
    ));
    private static final List<String> actionsForReaders = Collections.unmodifiableList(Arrays.asList( //
            "find", //
            "useUUID", //
            "viewRole", //
            "changeStream", //
            "collStats", //
            "dbHash", //
            "dbStats", //
            "indexStats", //
            "listCollections", //
            "listIndexes" //
    ));
    private static final List<String> actionsForWriters = Collections.unmodifiableList(Arrays.asList( //
            "insert", //
            "remove", //
            "update", //
            "bypassDocumentValidation", //
            "createCollection", //
            "createIndex", //
            "dropCollection", //
            "collMod", //
            "convertToCapped", //
            "dropIndex", //
            "reIndex", //
            "renameCollectionSameDB", //
            "listCollections", //
            "listIndexes" //
    ));

    private void addRoleForUser(Map<String, MongodbManagerConfigUserAndRoles> userAndRolesByName, List<MongodbManagerConfigUserAndRoles> usersPermissions, MongoDBUser mongoDBUser, String roleDatabase,
            String roleName) {
        MongodbManagerConfigUserAndRoles userAndRoles = userAndRolesByName.get(mongoDBUser.getName());
        if (userAndRoles == null) {
            userAndRoles = new MongodbManagerConfigUserAndRoles("admin", mongoDBUser.getName(), mongoDBUser.getPassword());
            userAndRoles.setRolesByDatabase(new HashMap<>());

            userAndRolesByName.put(mongoDBUser.getName(), userAndRoles);
            usersPermissions.add(userAndRoles);
        }

        CollectionsTools.getOrCreateEmptyArrayList(userAndRoles.getRolesByDatabase(), roleDatabase, String.class).add(roleName);
    }

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {
        List<ActionHandler> actions = new ArrayList<>();

        // Database
        ChangesEventHandlerResourceStream<MongoDBDatabase> dbStream = new ChangesEventHandlerResourceStream<>(MongoDBDatabase.class);
        dbStream.resourcesAddOfType(changesInTransactionContext.getLastAddedResources());
        dbStream.resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        dbStream.getResourcesStream() //
                .forEach(database -> {
                    actions.add((s, changes) -> {
                        // Validation
                        if (blacklistedDatabases.contains(database.getName().toLowerCase())) {
                            throw new IllegalUpdateException("That database name " + database.getName() + "is blacklisted");
                        }
                    });
                });

        // User
        ChangesEventHandlerResourceStream<MongoDBUser> usersStream = new ChangesEventHandlerResourceStream<>(MongoDBUser.class);
        usersStream.resourcesAddOfType(changesInTransactionContext.getLastAddedResources());
        usersStream.resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        usersStream.getResourcesStream() //
                .forEach(user -> {
                    actions.add((s, changes) -> {
                        // Validation
                        if (blacklistedUsers.contains(user.getName().toLowerCase())) {
                            throw new IllegalUpdateException("That database user name " + user.getName() + "is blacklisted");
                        }
                    });
                });

        // Add links
        usersStream.sortedAndDistinct();
        dbStream.resourcesAdd(usersStream.streamFromResourceAndLinkTypesAndToResourceClass(services, //
                new String[] { MongoDBUser.LINK_TYPE_ADMIN, MongoDBUser.LINK_TYPE_READ, MongoDBUser.LINK_TYPE_WRITE }, //
                MongoDBDatabase.class));
        dbStream.resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        dbStream.linksAddTo(changesInTransactionContext.getLastAddedLinks(), new String[] { MongoDBUser.LINK_TYPE_ADMIN, MongoDBUser.LINK_TYPE_READ, MongoDBUser.LINK_TYPE_WRITE });
        dbStream.linksAddTo(changesInTransactionContext.getLastDeletedLinks(), new String[] { MongoDBUser.LINK_TYPE_ADMIN, MongoDBUser.LINK_TYPE_READ, MongoDBUser.LINK_TYPE_WRITE });
        dbStream.sortedAndDistinct();

        // Server
        ChangesEventHandlerResourceStream<MongoDBServer> serversStream = dbStream.streamFromResourceAndLinkTypeAndToResourceClass(services, LinkTypeConstants.INSTALLED_ON, MongoDBServer.class);
        serversStream.resourcesAddOfType(changesInTransactionContext.getLastAddedResources());
        serversStream.resourcesAddOfType(changesInTransactionContext.getLastRefreshedResources());
        serversStream.resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        serversStream.linksAddFromAndTo(changesInTransactionContext.getLastAddedLinks());
        serversStream.linksAddFromAndTo(changesInTransactionContext.getLastDeletedLinks());

        serversStream.getResourcesStream() //
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

                        // Prepare the config
                        MongodbManagerConfig managerConfig = new MongodbManagerConfig();
                        managerConfig.setConnection(new JdbcUriConfigConnection().setJdbcUri("jdbc:mongodb://root:" + server.getRootPassword() + "@127.0.0.1:27017/"));
                        managerConfig.setDatabases(new ArrayList<>());
                        managerConfig.setUsersToIgnore(Arrays.asList(new MongodbManagerConfigUser("admin", "root")));

                        Map<String, List<MongodbManagerConfigCollectionPrivilege>> privilegesByRole = new HashMap<>();
                        privilegesByRole.put(ROLE_NAME_ADMIN, Collections.singletonList(new MongodbManagerConfigCollectionPrivilege("", actionsForAdmin)));
                        privilegesByRole.put(ROLE_NAME_READER, Collections.singletonList(new MongodbManagerConfigCollectionPrivilege("", actionsForReaders)));
                        privilegesByRole.put(ROLE_NAME_WRITER, Collections.singletonList(new MongodbManagerConfigCollectionPrivilege("", actionsForWriters)));

                        Map<String, MongodbManagerConfigUserAndRoles> userAndRolesByName = new HashMap<>();

                        resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(MongoDBDatabase.class, LinkTypeConstants.INSTALLED_ON, server).forEach(mongoDBDatabase -> {
                            String databaseName = mongoDBDatabase.getName();
                            logger.debug("[{}] Has database {}", serverName, databaseName);
                            managerConfig.getDatabases().add(databaseName);

                            managerConfig.getRoleByDatabase().put(databaseName, privilegesByRole);

                            // ADMIN
                            resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(MongoDBUser.class, MongoDBUser.LINK_TYPE_ADMIN, mongoDBDatabase).forEach(mongoDBUser -> {
                                logger.debug("[{}] Database {} has user {} as ADMIN", serverName, databaseName, mongoDBUser.getName());
                                addRoleForUser(userAndRolesByName, managerConfig.getUsersPermissions(), mongoDBUser, databaseName, ROLE_NAME_ADMIN);
                            });

                            // READ
                            resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(MongoDBUser.class, MongoDBUser.LINK_TYPE_READ, mongoDBDatabase).forEach(mongoDBUser -> {
                                logger.debug("[{}] Database {} has user {} as READ", serverName, databaseName, mongoDBUser.getName());
                                addRoleForUser(userAndRolesByName, managerConfig.getUsersPermissions(), mongoDBUser, databaseName, ROLE_NAME_READER);
                            });

                            // WRITE
                            resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(MongoDBUser.class, MongoDBUser.LINK_TYPE_WRITE, mongoDBDatabase).forEach(mongoDBUser -> {
                                logger.debug("[{}] Database {} has user {} as WRITE", serverName, databaseName, mongoDBUser.getName());
                                addRoleForUser(userAndRolesByName, managerConfig.getUsersPermissions(), mongoDBUser, databaseName, ROLE_NAME_WRITER);
                            });

                        });

                        if (unixUsers.size() > 1) {
                            throw new ProblemException("Cannot run as more than 1 unix user");
                        }
                        if (machines.size() > 1) {
                            throw new ProblemException("Cannot be installed on multiple machines");
                        }
                        if (unixUsers.size() == 1) {

                            UnixUser unixUser = unixUsers.get(0);

                            // Server Application
                            {
                                Application application = ActionsHandlerUtils.getOrCreateAnApplication(resourceService, serverName);
                                desiredManageApplications.add(application);
                                application.setDescription(server.getDescription());

                                IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
                                application.setApplicationDefinition(applicationDefinition);

                                applicationDefinition.setFrom("foilen/fcloud-docker-mongodb:" + server.getVersion());

                                applicationDefinition.setCommand("/mongodb-start.sh");
                                IPApplicationDefinitionAssetsBundle assetsBundle = applicationDefinition.addAssetsBundle();
                                applicationDefinition.addContainerUserToChangeId("mongodb", unixUser.getId());

                                applicationDefinition.addPortEndpoint(27017, "MONGODB_TCP");

                                applicationDefinition.setRunAs(unixUser.getId());

                                // Data folder
                                if (unixUser.getHomeFolder() != null) {
                                    String baseFolder = unixUser.getHomeFolder() + "/mongodb/" + serverName;
                                    applicationDefinition.addVolume(new IPApplicationDefinitionVolume(baseFolder, "/var/lib/mongodb", unixUser.getId(), unixUser.getId(), "770"));
                                }

                                // Save the root password
                                String newPass = server.getRootPassword();
                                assetsBundle.addAssetContent("/newPass", newPass);

                                ActionsHandlerUtils.addOrUpdate(application, changes);

                                // Sync links
                                CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.INSTALLED_ON, Machine.class, machines);
                                CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.RUN_AS, UnixUser.class, unixUsers);
                            }

                            // Manage Application
                            if (!machines.isEmpty()) {

                                Application application = ActionsHandlerUtils.getOrCreateAnApplication(resourceService, serverName + "_manager");
                                desiredManageApplications.add(application);
                                application.setDescription(server.getDescription());

                                IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
                                application.setApplicationDefinition(applicationDefinition);

                                applicationDefinition.setFrom("foilen/database-tools:0.4.1");

                                applicationDefinition.setCommand("/app/bin/database-tools mongodb-manage --configFiles /manager-config.json --keepAlive");

                                applicationDefinition.addPortRedirect(27017, machines.get(0).getName(), serverName, "MONGODB_TCP");

                                applicationDefinition.setRunAs(unixUser.getId());

                                // Save the config
                                applicationDefinition.addCopyWhenStartedContent("/manager-config.json", JsonTools.prettyPrint(managerConfig));

                                ActionsHandlerUtils.addOrUpdate(application, changes);

                                // Sync links
                                CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.INSTALLED_ON, Machine.class, machines);
                                CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.RUN_AS, UnixUser.class, unixUsers);

                            }

                        }

                        CommonResourceLink.syncToLinks(services, changes, server, LinkTypeConstants.MANAGES, Application.class, desiredManageApplications);

                    });

                });

        return actions;
    }

}
