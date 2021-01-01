/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.website;

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

public class WebsiteManageUnixUsersActionHandler extends AbstractBasics implements ActionHandler {

    private String websiteName;

    public WebsiteManageUnixUsersActionHandler(String websiteName) {
        this.websiteName = websiteName;
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing {}", websiteName);

        IPResourceService resourceService = services.getResourceService();
        Optional<Website> o = resourceService.resourceFindByPk(new Website(websiteName));
        if (!o.isPresent()) {
            logger.info("{} is not present. Skipping", websiteName);
            return;
        }
        Website Website = o.get();

        List<UnixUser> managedUnixUsers = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(Website, LinkTypeConstants.MANAGES, UnixUser.class);

        String unixUserWebsiteName = WebsiteChangesEventHandler.UNIX_USER_HA_PROXY_NAME;
        managedUnixUsers.stream() //
                .filter(uu -> !StringTools.safeEquals(uu.getName(), unixUserWebsiteName)) //
                .forEach(uu -> {
                    logger.info("Remove the unix user {} as managed", unixUserWebsiteName);
                    changes.linkDelete(Website, LinkTypeConstants.MANAGES, uu);
                });
        if (managedUnixUsers.isEmpty()) {
            Optional<UnixUser> unixUserOptional = resourceService.resourceFind(resourceService.createResourceQuery(UnixUser.class).propertyEquals(UnixUser.PROPERTY_NAME, unixUserWebsiteName));
            UnixUser unixUser;
            if (unixUserOptional.isPresent()) {
                unixUser = unixUserOptional.get();
            } else {
                logger.info("Could not find the unix user {}. Will create it", unixUserWebsiteName);
                unixUser = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), unixUserWebsiteName, "/home/" + unixUserWebsiteName, null, null);
                changes.resourceAdd(unixUser);
            }
            logger.info("Add the unix user {} as managed by the website", unixUserWebsiteName);
            changes.linkAdd(Website, LinkTypeConstants.MANAGES, unixUser);
        }

    }

}
