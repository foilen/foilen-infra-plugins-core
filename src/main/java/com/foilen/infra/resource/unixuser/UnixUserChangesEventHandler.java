/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.unixuser;

import java.util.ArrayList;
import java.util.List;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.unixuser.helper.UnixUserAvailableIdHelper;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StringTools;

public class UnixUserChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        List<ActionHandler> actions = new ArrayList<>();

        // When adding, validate, set and id
        ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), UnixUser.class) //
                .forEach(unixUser -> {

                    actions.add((s, changes) -> {

                        logger.info("Processing new UnixUser {}", unixUser.getName());

                        // Unique user name
                        IPResourceService resourceService = services.getResourceService();
                        List<UnixUser> unixUsers = resourceService.resourceFindAll(resourceService.createResourceQuery(UnixUser.class) //
                                .propertyEquals(UnixUser.PROPERTY_NAME, unixUser.getName()));
                        if (unixUsers.size() > 1) {
                            throw new IllegalUpdateException("Unix User name " + unixUser.getName() + " is already used");
                        }

                        // Choose the next id
                        if (unixUser.getId() == null) {
                            unixUser.setId(UnixUserAvailableIdHelper.getNextAvailableId());
                            logger.debug("Setting unix user id {}", unixUser.getInternalId());
                            changes.resourceUpdate(unixUser.getInternalId(), unixUser);
                        }

                    });

                    actions.add(new UnixUserFixFieldsActionHandler(unixUser));

                });

        // When updated, check unique user name
        ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), UnixUser.class) //
                .forEach(updatedResource -> {

                    UnixUser unixUser = (UnixUser) updatedResource.getNext();

                    actions.add((s, changes) -> {

                        logger.info("Processing update UnixUser {}", unixUser.getName());

                        UnixUser previousResource = (UnixUser) updatedResource.getPrevious();
                        UnixUser newResource = (UnixUser) updatedResource.getNext();

                        // Unique user name
                        if (!StringTools.safeEquals(previousResource.getName(), newResource.getName())) {
                            IPResourceService resourceService = services.getResourceService();
                            List<UnixUser> unixUsers = resourceService.resourceFindAll(resourceService.createResourceQuery(UnixUser.class) //
                                    .propertyEquals(UnixUser.PROPERTY_NAME, unixUser.getName()));
                            if (unixUsers.size() > 1) {
                                throw new IllegalUpdateException("Unix User name " + unixUser.getName() + " is already used");
                            }
                        }

                    });

                    actions.add(new UnixUserFixFieldsActionHandler(unixUser));

                });

        return actions;
    }

}
