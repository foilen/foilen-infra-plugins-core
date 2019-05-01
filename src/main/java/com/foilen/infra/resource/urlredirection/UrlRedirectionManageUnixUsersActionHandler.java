/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.urlredirection;

import java.util.List;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.unixuser.helper.UnixUserAvailableIdHelper;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StringTools;

/**
 * For each UrlRedirection:
 * <ul>
 * <li>Manage the common unix user</li>
 * </ul>
 *
 */
public class UrlRedirectionManageUnixUsersActionHandler extends AbstractBasics implements ActionHandler {

    private String urlRedirectionDomainName;

    public UrlRedirectionManageUnixUsersActionHandler(String urlRedirectionDomainName) {
        this.urlRedirectionDomainName = urlRedirectionDomainName;
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing {}", urlRedirectionDomainName);

        IPResourceService resourceService = services.getResourceService();
        Optional<UrlRedirection> o = resourceService.resourceFindByPk(new UrlRedirection(urlRedirectionDomainName));
        if (!o.isPresent()) {
            logger.info("{} is not present. Skipping", urlRedirectionDomainName);
            return;
        }
        UrlRedirection urlRedirection = o.get();

        List<UnixUser> managedUnixUsers = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(urlRedirection, LinkTypeConstants.MANAGES, UnixUser.class);

        String unixUserRedirectionName = UrlRedirectionChangesEventHandler.UNIX_USER_REDIRECTION_NAME;
        managedUnixUsers.stream() //
                .filter(uu -> !StringTools.safeEquals(uu.getName(), unixUserRedirectionName)) //
                .forEach(uu -> {
                    logger.info("Remove the unix user {} as managed", unixUserRedirectionName);
                    changes.linkDelete(urlRedirection, LinkTypeConstants.MANAGES, uu);
                });
        if (managedUnixUsers.isEmpty()) {
            Optional<UnixUser> unixUserOptional = resourceService.resourceFind(resourceService.createResourceQuery(UnixUser.class).propertyEquals(UnixUser.PROPERTY_NAME, unixUserRedirectionName));
            UnixUser unixUser;
            if (unixUserOptional.isPresent()) {
                unixUser = unixUserOptional.get();
            } else {
                logger.info("Could not find the unix user {}. Will create it", unixUserRedirectionName);
                unixUser = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), unixUserRedirectionName, "/home/" + unixUserRedirectionName, null, null);
                changes.resourceAdd(unixUser);
            }
            logger.info("Add the unix user {} as managed", unixUserRedirectionName);
            changes.linkAdd(urlRedirection, LinkTypeConstants.MANAGES, unixUser);
        }

    }

}
