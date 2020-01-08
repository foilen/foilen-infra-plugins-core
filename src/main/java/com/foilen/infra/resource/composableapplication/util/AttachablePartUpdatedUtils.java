/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.util;

import java.util.stream.Stream;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerResourceStream;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.ComposableApplication;
import com.foilen.infra.resource.composableapplication.parts.AttachableContainerUserToChangeId;
import com.foilen.infra.resource.composableapplication.parts.AttachableMariaDB;
import com.foilen.infra.resource.composableapplication.parts.AttachableMongoDB;
import com.foilen.infra.resource.composableapplication.parts.AttachablePostgreSql;
import com.foilen.infra.resource.composableapplication.parts.AttachableService;
import com.foilen.infra.resource.email.resources.AttachableEmailRelayToMsmtpConfigFile;
import com.foilen.infra.resource.email.resources.AttachableEmailRelayToSendmail;
import com.foilen.infra.resource.email.resources.EmailRelay;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.mariadb.MariaDBServer;
import com.foilen.infra.resource.mongodb.MongoDBServer;
import com.foilen.infra.resource.postgresql.PostgreSqlServer;
import com.foilen.infra.resource.unixuser.UnixUser;

public class AttachablePartUpdatedUtils {

    /**
     * Get a stream of all the resources of the specified type that has an AttachablePart that was updated in any way in the last run.
     *
     * @param <T>
     *            the resource type
     * @param services
     *            the services
     * @param changesInTransactionContext
     *            the current changes
     * @param resourceType
     *            the resource type
     * @return the stream of the resources
     */
    public static <T extends IPResource> Stream<T> lastChanges(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext, Class<T> resourceType) {

        // Updated AttachablePart that is linked to resourceType
        ChangesEventHandlerResourceStream<AttachablePart> attachablePartStream = new ChangesEventHandlerResourceStream<>(AttachablePart.class);
        attachablePartStream.resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        attachablePartStream.linksAddFromAndTo(changesInTransactionContext.getLastAddedLinks());
        attachablePartStream.linksAddFromAndTo(changesInTransactionContext.getLastDeletedLinks());
        ChangesEventHandlerResourceStream<T> resourceStream = attachablePartStream.streamFromResourceClassAndLinkType(services, resourceType, ComposableApplication.LINK_TYPE_ATTACHED);

        ChangesEventHandlerResourceStream<UnixUser> updatedUnixUser = new ChangesEventHandlerResourceStream<>(UnixUser.class) //
                .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());

        // AttachableCronJob : nothing

        // AttachableMariaDB (if INSTALLED_ON machine changes)
        resourceStream.resourcesAdd(new ChangesEventHandlerResourceStream<>(MariaDBServer.class) //
                .linksAddFrom(changesInTransactionContext.getLastAddedLinks(), LinkTypeConstants.INSTALLED_ON, Machine.class) //
                .linksAddFrom(changesInTransactionContext.getLastDeletedLinks(), LinkTypeConstants.INSTALLED_ON, Machine.class) //
                .streamFromResourceClassAndLinkType(services, AttachableMariaDB.class, LinkTypeConstants.POINTS_TO) //
                .streamFromResourceClassAndLinkType(services, resourceType, ComposableApplication.LINK_TYPE_ATTACHED) //
        );

        // AttachableContainerUserToChangeId (if USES unixUser changes)
        resourceStream.resourcesAdd(updatedUnixUser //
                .streamFromResourceClassAndLinkType(services, AttachableContainerUserToChangeId.class, LinkTypeConstants.USES) //
                .streamFromResourceClassAndLinkType(services, resourceType, ComposableApplication.LINK_TYPE_ATTACHED) //
        );

        // AttachablePostgreSql (if INSTALLED_ON machine changes)
        resourceStream.resourcesAdd(new ChangesEventHandlerResourceStream<>(PostgreSqlServer.class) //
                .linksAddFrom(changesInTransactionContext.getLastAddedLinks(), LinkTypeConstants.INSTALLED_ON, Machine.class) //
                .linksAddFrom(changesInTransactionContext.getLastDeletedLinks(), LinkTypeConstants.INSTALLED_ON, Machine.class) //
                .streamFromResourceClassAndLinkType(services, AttachablePostgreSql.class, LinkTypeConstants.POINTS_TO) //
                .streamFromResourceClassAndLinkType(services, resourceType, ComposableApplication.LINK_TYPE_ATTACHED) //
        );

        // AttachableMongoDB (if INSTALLED_ON machine changes)
        resourceStream.resourcesAdd(new ChangesEventHandlerResourceStream<>(MongoDBServer.class) //
                .linksAddFrom(changesInTransactionContext.getLastAddedLinks(), LinkTypeConstants.INSTALLED_ON, Machine.class) //
                .linksAddFrom(changesInTransactionContext.getLastDeletedLinks(), LinkTypeConstants.INSTALLED_ON, Machine.class) //
                .streamFromResourceClassAndLinkType(services, AttachableMongoDB.class, LinkTypeConstants.POINTS_TO) //
                .streamFromResourceClassAndLinkType(services, resourceType, ComposableApplication.LINK_TYPE_ATTACHED) //
        );

        // AttachableVolume : nothing

        // AttachableService (if RUN_AS unixUser changes)
        resourceStream.resourcesAdd(updatedUnixUser //
                .streamFromResourceClassAndLinkType(services, AttachableService.class, LinkTypeConstants.RUN_AS) //
                .streamFromResourceClassAndLinkType(services, resourceType, ComposableApplication.LINK_TYPE_ATTACHED) //
        );

        // AttachableAptInstall : nothing

        // AttachableEmailRelayToMsmtpConfigFile (if POINTS_TO emailRelay changes)
        ChangesEventHandlerResourceStream<EmailRelay> updatedEmailRelay = new ChangesEventHandlerResourceStream<>(EmailRelay.class) //
                .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        resourceStream.resourcesAdd(updatedEmailRelay //
                .streamFromResourceClassAndLinkType(services, AttachableEmailRelayToMsmtpConfigFile.class, LinkTypeConstants.POINTS_TO) //
                .streamFromResourceClassAndLinkType(services, resourceType, ComposableApplication.LINK_TYPE_ATTACHED) //
        );

        // AttachableEmailRelayToSendmail (if POINTS_TO emailRelay changes)
        resourceStream.resourcesAdd(updatedEmailRelay //
                .streamFromResourceClassAndLinkType(services, AttachableEmailRelayToSendmail.class, LinkTypeConstants.POINTS_TO) //
                .streamFromResourceClassAndLinkType(services, resourceType, ComposableApplication.LINK_TYPE_ATTACHED) //
        );

        return resourceStream.getResourcesStream();
    }

}
