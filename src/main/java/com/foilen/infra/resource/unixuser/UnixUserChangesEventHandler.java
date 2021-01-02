/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.unixuser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.TranslationService;
import com.foilen.infra.resource.unixuser.helper.UnixUserAvailableIdHelper;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StringTools;
import com.google.common.base.Strings;

public class UnixUserChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    private static Pattern alphaNumLowerValidationRegex = Pattern.compile("[a-z0-9\\_\\-]+");

    private void assertValidName(TranslationService translationService, String unixUserName) {
        if (Strings.isNullOrEmpty(unixUserName)) {
            throw new IllegalUpdateException("You must put a user name");
        }
        if (unixUserName.length() > 32) {
            throw new IllegalUpdateException("Max unix user name length is 32");
        }
        if (!alphaNumLowerValidationRegex.matcher(unixUserName).matches()) {
            throw new IllegalUpdateException(translationService.translate("error.nameValid"));
        }
    }

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        List<ActionHandler> actions = new ArrayList<>();

        // When adding, validate, set and id
        ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), UnixUser.class) //
                .forEach(unixUser -> {

                    actions.add((s, changes) -> {

                        String unixUserName = unixUser.getName();
                        logger.info("Processing new UnixUser {}", unixUserName);

                        // Valid name
                        assertValidName(services.getTranslationService(), unixUserName);

                        // Unique user name
                        IPResourceService resourceService = services.getResourceService();
                        List<UnixUser> unixUsers = resourceService.resourceFindAll(resourceService.createResourceQuery(UnixUser.class) //
                                .propertyEquals(UnixUser.PROPERTY_NAME, unixUserName));
                        if (unixUsers.size() > 1) {
                            throw new IllegalUpdateException("Unix User name " + unixUserName + " is already used");
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

                        String unixUserName = unixUser.getName();
                        logger.info("Processing update UnixUser {}", unixUserName);

                        UnixUser previousResource = (UnixUser) updatedResource.getPrevious();
                        UnixUser newResource = (UnixUser) updatedResource.getNext();

                        // Valid name
                        assertValidName(services.getTranslationService(), newResource.getName());

                        // Unique user name
                        if (!StringTools.safeEquals(previousResource.getName(), newResource.getName())) {
                            IPResourceService resourceService = services.getResourceService();
                            List<UnixUser> unixUsers = resourceService.resourceFindAll(resourceService.createResourceQuery(UnixUser.class) //
                                    .propertyEquals(UnixUser.PROPERTY_NAME, unixUserName));
                            if (unixUsers.size() > 1) {
                                throw new IllegalUpdateException("Unix User name " + unixUserName + " is already used");
                            }
                        }

                    });

                    actions.add(new UnixUserFixFieldsActionHandler(unixUser));

                });

        return actions;
    }

}
