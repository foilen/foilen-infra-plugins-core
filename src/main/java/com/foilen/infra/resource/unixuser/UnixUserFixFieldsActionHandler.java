/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

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
                logger.info("Updating the hashed password");
                unixUser.setHashedPassword(Sha2Crypt.sha512Crypt(unixUser.getPassword().getBytes(CharsetTools.UTF_8)));
                changes.resourceUpdate(unixUser);
            }

            // Clear the password if desired
            if (!unixUser.isKeepClearPassword()) {
                logger.info("Remove the clear password");
                unixUser.setPassword(null);
                changes.resourceUpdate(unixUser);
            }
        }

        // Set home folder
        String expectedHomeFolder = "/home/" + unixUser.getName();
        if (unixUser.getHomeFolder() == null) {
            logger.info("Setting home folder to {}", expectedHomeFolder);
            unixUser.setHomeFolder(expectedHomeFolder);
            changes.resourceUpdate(unixUser);
        } else {
            if (!(unixUser instanceof SystemUnixUser) && !StringTools.safeEquals(expectedHomeFolder, unixUser.getHomeFolder())) {
                throw new IllegalUpdateException("Expected home folder is " + expectedHomeFolder);
            }
        }

        // Set Shell
        if (unixUser.getShell() == null) {
            unixUser.setShell("/bin/bash");
            logger.info("Setting shell to {}", unixUser.getShell());
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
