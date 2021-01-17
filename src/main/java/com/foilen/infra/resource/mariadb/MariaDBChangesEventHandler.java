/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mariadb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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
import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.mariadb.mysqlmanager.MysqlManagerConfig;
import com.foilen.infra.resource.mariadb.mysqlmanager.MysqlManagerConfigAdmin;
import com.foilen.infra.resource.mariadb.mysqlmanager.MysqlManagerConfigDatabaseGrants;
import com.foilen.infra.resource.mariadb.mysqlmanager.MysqlManagerConfigPermission;
import com.foilen.infra.resource.mariadb.mysqlmanager.MysqlManagerConfigUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.utils.ActionsHandlerUtils;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.google.common.base.Strings;

public class MariaDBChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    private static final List<String> blacklistedDatabases = Collections.unmodifiableList(Arrays.asList( //
            "information_schema", //
            "mysql", //
            "performance_schema", //
            "sys" //
    ));

    private static final List<String> blacklistedUsers = Collections.unmodifiableList(Arrays.asList( //
            "root" //
    ));

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {
        List<ActionHandler> actions = new ArrayList<>();

        // Database
        ChangesEventHandlerResourceStream<MariaDBDatabase> dbStream = new ChangesEventHandlerResourceStream<>(MariaDBDatabase.class);
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
        ChangesEventHandlerResourceStream<MariaDBUser> usersStream = new ChangesEventHandlerResourceStream<>(MariaDBUser.class);
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
                new String[] { MariaDBUser.LINK_TYPE_ADMIN, MariaDBUser.LINK_TYPE_READ, MariaDBUser.LINK_TYPE_WRITE }, //
                MariaDBDatabase.class));
        dbStream.resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        dbStream.linksAddTo(changesInTransactionContext.getLastAddedLinks(), new String[] { MariaDBUser.LINK_TYPE_ADMIN, MariaDBUser.LINK_TYPE_READ, MariaDBUser.LINK_TYPE_WRITE });
        dbStream.linksAddTo(changesInTransactionContext.getLastDeletedLinks(), new String[] { MariaDBUser.LINK_TYPE_ADMIN, MariaDBUser.LINK_TYPE_READ, MariaDBUser.LINK_TYPE_WRITE });
        dbStream.sortedAndDistinct();
        dbStream.getResourcesStream() //
                .forEach(database -> {
                    actions.add((s, changes) -> {
                        // Validation - Unique users names
                        Map<String, String> uidByUserName = new HashMap<>();

                        Consumer<MariaDBUser> validateUserNamesAndUids = user -> {
                            String previousUid = uidByUserName.put(user.getName(), user.getUid());
                            if (previousUid != null && !previousUid.equals(user.getUid())) {
                                throw new IllegalUpdateException("That database user name " + user.getName() + " is already used on that database");
                            }
                        };

                        services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(MariaDBUser.class, MariaDBUser.LINK_TYPE_ADMIN, database) //
                                .forEach(validateUserNamesAndUids);
                        services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(MariaDBUser.class, MariaDBUser.LINK_TYPE_READ, database) //
                                .forEach(validateUserNamesAndUids);
                        services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(MariaDBUser.class, MariaDBUser.LINK_TYPE_WRITE, database) //
                                .forEach(validateUserNamesAndUids);

                    });
                });

        // Server
        ChangesEventHandlerResourceStream<MariaDBServer> serversStream = dbStream.streamFromResourceAndLinkTypeAndToResourceClass(services, LinkTypeConstants.INSTALLED_ON, MariaDBServer.class);
        serversStream.resourcesAddOfType(changesInTransactionContext.getLastAddedResources());
        serversStream.resourcesAddOfType(changesInTransactionContext.getLastRefreshedResources());
        serversStream.resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        serversStream.linksAddFromAndTo(changesInTransactionContext.getLastAddedLinks());
        serversStream.linksAddFromAndTo(changesInTransactionContext.getLastDeletedLinks());

        // Unix user that changed
        ChangesEventHandlerResourceStream<UnixUser> unixUserStream = new ChangesEventHandlerResourceStream<>(UnixUser.class);
        unixUserStream.resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        unixUserStream.getResourcesStream() //
                .forEach(unixUser -> {
                    serversStream.resourcesAdd(services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(MariaDBServer.class, LinkTypeConstants.RUN_AS, unixUser));
                });

        serversStream.getResourcesStream() //
                .map(it -> it.getName()) //
                .sorted().distinct() //
                .forEach(serverName -> {

                    actions.add((s, changes) -> {

                        logger.info("Processing mariadb server {}", serverName);

                        IPResourceService resourceService = services.getResourceService();
                        Optional<MariaDBServer> o = resourceService.resourceFindByPk(new MariaDBServer(serverName));
                        if (!o.isPresent()) {
                            logger.info("{} is not present. Skipping", serverName);
                            return;
                        }
                        MariaDBServer server = o.get();

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
                        MysqlManagerConfig mysqlManagerConfig = new MysqlManagerConfig();
                        mysqlManagerConfig.setAdmin(new MysqlManagerConfigAdmin("root", server.getRootPassword()));
                        mysqlManagerConfig.getUsersToIgnore().add(new MysqlManagerConfigUser("root", "localhost"));
                        mysqlManagerConfig.getUsersToIgnore().add(new MysqlManagerConfigUser("root", "%"));
                        mysqlManagerConfig.getUsersToIgnore().add(new MysqlManagerConfigUser("mariadb.sys", "localhost"));

                        Map<String, MysqlManagerConfigPermission> userConfigByName = new HashMap<>();

                        // All databases
                        Map<String, String> uidByDatabaseName = new HashMap<>();

                        Consumer<MariaDBDatabase> validateDatabaseNamesAndUids = database -> {
                            String previousUid = uidByDatabaseName.put(database.getName(), database.getUid());
                            if (previousUid != null && !previousUid.equals(database.getUid())) {
                                throw new IllegalUpdateException("That database name " + database.getName() + " is already used on that server");
                            }
                        };

                        services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(MariaDBDatabase.class, LinkTypeConstants.INSTALLED_ON, server).forEach(mariaDBDatabase -> {

                            String databaseName = mariaDBDatabase.getName();
                            logger.debug("[{}] Has database {}", serverName, databaseName);
                            mysqlManagerConfig.getDatabases().add(databaseName);

                            // Validate
                            validateDatabaseNamesAndUids.accept(mariaDBDatabase);

                            // ADMIN
                            services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(MariaDBUser.class, MariaDBUser.LINK_TYPE_ADMIN, mariaDBDatabase)
                                    .forEach(mariaDBUser -> {
                                        logger.debug("[{}] Database {} has user {} as ADMIN", serverName, databaseName, mariaDBUser.getName());
                                        List<String> grants = getGrantsByUserAndDatabase(userConfigByName, mariaDBUser, databaseName);
                                        grants.add("ALTER");
                                        grants.add("CREATE");
                                        grants.add("CREATE ROUTINE");
                                        grants.add("CREATE TEMPORARY TABLES");
                                        grants.add("CREATE VIEW");
                                        grants.add("DROP");
                                        grants.add("EVENT");
                                        grants.add("INDEX");
                                        grants.add("LOCK TABLES");
                                        grants.add("SHOW VIEW");
                                        grants.add("TRIGGER");
                                    });

                            // READ
                            services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(MariaDBUser.class, MariaDBUser.LINK_TYPE_READ, mariaDBDatabase)
                                    .forEach(mariaDBUser -> {
                                        logger.debug("[{}] Database {} has user {} as READ", serverName, databaseName, mariaDBUser.getName());
                                        List<String> grants = getGrantsByUserAndDatabase(userConfigByName, mariaDBUser, databaseName);
                                        grants.add("SELECT");
                                    });

                            // WRITE
                            services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(MariaDBUser.class, MariaDBUser.LINK_TYPE_WRITE, mariaDBDatabase)
                                    .forEach(mariaDBUser -> {
                                        logger.debug("[{}] Database {} has user {} as WRITE", serverName, databaseName, mariaDBUser.getName());
                                        List<String> grants = getGrantsByUserAndDatabase(userConfigByName, mariaDBUser, databaseName);
                                        grants.add("INSERT");
                                        grants.add("UPDATE");
                                        grants.add("DELETE");
                                    });

                        });

                        // Apply users permissions
                        userConfigByName.values().forEach(userConfig -> {
                            mysqlManagerConfig.getUsersPermissions().add(userConfig);
                        });

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

                            applicationDefinition.setFrom("foilen/fcloud-docker-mariadb:" + server.getVersion());

                            applicationDefinition.addService("app", "/mariadb-start.sh");
                            IPApplicationDefinitionAssetsBundle assetsBundle = applicationDefinition.addAssetsBundle();
                            applicationDefinition.addContainerUserToChangeId("mysql", unixUser.getId());

                            applicationDefinition.addPortEndpoint(3306, DockerContainerEndpoints.MYSQL_TCP);

                            applicationDefinition.setRunAs(unixUser.getId());

                            // Configuration
                            applicationDefinition.addAssetResource("/etc/mysql/conf.d/zInfra.cnf", "/com/foilen/infra/resource/mariadb/config.cnf");

                            // Data folder
                            if (unixUser.getHomeFolder() != null) {
                                String baseFolder = unixUser.getHomeFolder() + "/mysql/" + serverName;
                                applicationDefinition.addVolume(new IPApplicationDefinitionVolume(baseFolder + "/data", "/var/lib/mysql", unixUser.getId(), unixUser.getId(), "770"));

                                // Run folder
                                applicationDefinition.addVolume(new IPApplicationDefinitionVolume(baseFolder + "/run", "/var/run/mysqld/", unixUser.getId(), unixUser.getId(), "770"));

                                // Save the root password
                                applicationDefinition.addVolume(new IPApplicationDefinitionVolume(baseFolder + "/config", "/volumes/config/", unixUser.getId(), unixUser.getId(), "770"));
                                String newPass = server.getRootPassword();
                                assetsBundle.addAssetContent("/newPass", newPass);
                                assetsBundle.addAssetContent("/newPass.cnf", "[client]\npassword=" + newPass);
                            }

                            // Save the database config for the manager
                            applicationDefinition.addCopyWhenStartedContent("/manager-config.json", JsonTools.prettyPrint(mysqlManagerConfig));
                            applicationDefinition.addExecuteWhenStartedCommand("/mariadb-update-manager.sh");

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

    private List<String> getGrantsByUserAndDatabase(Map<String, MysqlManagerConfigPermission> userConfigByName, MariaDBUser mariaDBUser, String databaseName) {
        MysqlManagerConfigPermission userConfig = userConfigByName.get(mariaDBUser.getName());
        if (userConfig == null) {
            userConfig = new MysqlManagerConfigPermission(mariaDBUser.getName(), "%", mariaDBUser.getPassword());
            userConfigByName.put(mariaDBUser.getName(), userConfig);
        }

        Optional<MysqlManagerConfigDatabaseGrants> grantsOptional = userConfig.getDatabaseGrants().stream().filter(it -> databaseName.equals(it.getDatabaseName())).findAny();
        List<String> grants;
        if (grantsOptional.isPresent()) {
            grants = grantsOptional.get().getGrants();
        } else {
            MysqlManagerConfigDatabaseGrants databaseGrants = new MysqlManagerConfigDatabaseGrants(databaseName);
            grants = databaseGrants.getGrants();
            userConfig.getDatabaseGrants().add(databaseGrants);
        }
        return grants;

    }

}
