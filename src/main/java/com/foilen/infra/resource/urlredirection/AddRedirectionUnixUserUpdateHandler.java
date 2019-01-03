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
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.unixuser.helper.UnixUserAvailableIdHelper;
import com.foilen.smalltools.tuple.Tuple3;

/**
 * Create the redirection user if missing.
 */
public class AddRedirectionUnixUserUpdateHandler extends AbstractUpdateEventHandler<Machine> {

    public static final String UNIX_USER_REDIRECTION_NAME = "infra_url_redirection";

    @Override
    public void addHandler(CommonServicesContext services, ChangesContext changes, Machine resource) {
        createUnixUserIfMissing(services.getResourceService(), changes);
    }

    @Override
    public void checkAndFix(CommonServicesContext services, ChangesContext changes, Machine resource) {
    }

    protected void createUnixUserIfMissing(IPResourceService resourceService, ChangesContext changes) {
        Optional<UnixUser> unixUserOptional = resourceService.resourceFind(resourceService.createResourceQuery(UnixUser.class).propertyEquals(UnixUser.PROPERTY_NAME, UNIX_USER_REDIRECTION_NAME));
        if (!unixUserOptional.isPresent()) {
            logger.info("Could not find the unix user {}. Will create it", UNIX_USER_REDIRECTION_NAME);
            UnixUser unixUser = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), UNIX_USER_REDIRECTION_NAME, "/home/" + UNIX_USER_REDIRECTION_NAME, null, null);
            changes.resourceAdd(unixUser);
        }
    }

    @Override
    public void deleteHandler(CommonServicesContext services, ChangesContext changes, Machine resource, List<Tuple3<IPResource, String, IPResource>> previousLinks) {
    }

    @Override
    public Class<Machine> supportedClass() {
        return Machine.class;
    }

    @Override
    public void updateHandler(CommonServicesContext services, ChangesContext changes, Machine previousResource, Machine newResource) {
        createUnixUserIfMissing(services.getResourceService(), changes);
    }

}
