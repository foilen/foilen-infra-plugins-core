/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.unixuser;

import org.apache.commons.codec.digest.Sha2Crypt;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CharsetTools;
import com.foilen.smalltools.tools.StringTools;

public class UnixUserFixFieldsActionHandler extends AbstractBasics implements ActionHandler {

    private UnixUser unixUser;

    public UnixUserFixFieldsActionHandler(UnixUser unixUser) {
        this.unixUser = unixUser;
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing UnixUser {}", unixUser.getName());

        // Update hashed password and clear the password if requested
        if (unixUser.getPassword() != null) {

            boolean updateHash = false;

            if (unixUser.getHashedPassword() == null) {
                updateHash = true;
            } else {
                // Check if the hashed password is already a right one
                String expectedHash = Sha2Crypt.sha512Crypt(unixUser.getPassword().getBytes(CharsetTools.UTF_8), unixUser.getHashedPassword());
                if (!StringTools.safeEquals(expectedHash, unixUser.getHashedPassword())) {
                    updateHash = true;
                }
            }
            if (updateHash) {
                unixUser.setHashedPassword(Sha2Crypt.sha512Crypt(unixUser.getPassword().getBytes(CharsetTools.UTF_8)));
                changes.resourceUpdate(unixUser);
            }

            // Clear the password if desired
            if (!unixUser.isKeepClearPassword()) {
                unixUser.setPassword(null);
                changes.resourceUpdate(unixUser);
            }
        }

        // Set home folder
        if (unixUser.getHomeFolder() == null) {
            unixUser.setHomeFolder("/home/" + unixUser.getName());
            changes.resourceUpdate(unixUser);
        }

        // Set Shell
        if (unixUser.getShell() == null) {
            unixUser.setShell("/bin/bash");
            changes.resourceUpdate(unixUser);
        }

        // Validate id is right per type
        if (unixUser instanceof SystemUnixUser) {
            if (unixUser.getId() >= 70000L) {
                throw new IllegalUpdateException("Id is higher than 70000, but it is a system unix user. It is " + unixUser.getId());
            }
        } else {
            if (unixUser.getId() < 70000L) {
                throw new IllegalUpdateException("Id is lower than 70000. It is " + unixUser.getId());
            }
        }

    }

}
