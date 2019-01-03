/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.unixuser;

import java.util.List;

import org.apache.commons.codec.digest.Sha2Crypt;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.resource.unixuser.helper.UnixUserAvailableIdHelper;
import com.foilen.smalltools.tools.CharsetTools;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tuple.Tuple3;

public class UnixUserUpdateHandler extends AbstractUpdateEventHandler<UnixUser> {

    @Override
    public void addHandler(CommonServicesContext services, ChangesContext changes, UnixUser resource) {
        // Unique user name
        checkUniqueName(services, resource.getName());

        // Choose the next id
        if (resource.getId() == null) {
            resource.setId(UnixUserAvailableIdHelper.getNextAvailableId());
            changes.resourceUpdate(resource.getInternalId(), resource);
        }

        common(services, changes, resource);

    }

    @Override
    public void checkAndFix(CommonServicesContext services, ChangesContext changes, UnixUser resource) {
    }

    private void checkUniqueName(CommonServicesContext services, String name) {
        IPResourceService resourceService = services.getResourceService();
        List<UnixUser> unixUsers = resourceService.resourceFindAll(resourceService.createResourceQuery(UnixUser.class) //
                .propertyEquals(UnixUser.PROPERTY_NAME, name));
        if (unixUsers.size() > 1) {
            throw new IllegalUpdateException("Unix User name " + name + " is already used");
        }
    }

    private void common(CommonServicesContext services, ChangesContext changes, UnixUser resource) {
        // Update hashed password and clear the password if requested
        if (resource.getPassword() != null) {

            boolean updateHash = false;

            if (resource.getHashedPassword() == null) {
                updateHash = true;
            } else {
                // Check if the hashed password is already a right one
                String expectedHash = Sha2Crypt.sha512Crypt(resource.getPassword().getBytes(CharsetTools.UTF_8), resource.getHashedPassword());
                if (!StringTools.safeEquals(expectedHash, resource.getHashedPassword())) {
                    updateHash = true;
                }
            }
            if (updateHash) {
                resource.setHashedPassword(Sha2Crypt.sha512Crypt(resource.getPassword().getBytes(CharsetTools.UTF_8)));
                changes.resourceUpdate(resource);
            }

            // Clear the password if desired
            if (!resource.isKeepClearPassword()) {
                resource.setPassword(null);
                changes.resourceUpdate(resource);
            }
        }

        // Set home folder
        if (resource.getHomeFolder() == null) {
            resource.setHomeFolder("/home/" + resource.getName());
            changes.resourceUpdate(resource);
        }

        // Set Shell
        if (resource.getShell() == null) {
            resource.setShell("/bin/bash");
            changes.resourceUpdate(resource);
        }

        // Validate id is right per type
        if (resource instanceof SystemUnixUser) {
            if (resource.getId() >= 70000L) {
                throw new IllegalUpdateException("Id is higher than 70000, but it is a system unix user. It is " + resource.getId());
            }
        } else {
            if (resource.getId() < 70000L) {
                throw new IllegalUpdateException("Id is lower than 70000. It is " + resource.getId());
            }
        }

    }

    @Override
    public void deleteHandler(CommonServicesContext services, ChangesContext changes, UnixUser resource, List<Tuple3<IPResource, String, IPResource>> previousLinks) {
    }

    @Override
    public Class<UnixUser> supportedClass() {
        return UnixUser.class;
    }

    @Override
    public void updateHandler(CommonServicesContext services, ChangesContext changes, UnixUser previousResource, UnixUser newResource) {
        // Unique user name
        if (!StringTools.safeEquals(previousResource.getName(), newResource.getName())) {
            checkUniqueName(services, newResource.getName());
        }

        common(services, changes, newResource);
    }

}
