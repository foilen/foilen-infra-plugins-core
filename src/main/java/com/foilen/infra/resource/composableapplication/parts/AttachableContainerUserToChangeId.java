/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.parts;

import java.util.List;

import org.slf4j.Logger;

import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.AttachablePartContext;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tools.SecureRandomTools;

/**
 * To add a user id that needs to change inside the container.
 *
 * Links from:
 * <ul>
 * <li>{@link UnixUser}: (one) USES - The user id to change to</li>
 * </ul>
 */
public class AttachableContainerUserToChangeId extends AttachablePart {

    public static final String RESOURCE_TYPE = "Attachable Container User To Change Id";

    public static final String PROPERTY_UID = "uid";
    public static final String PROPERTY_USERNAME_IN_CONTAINER = "usernameInContainer";

    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(AttachableContainerUserToChangeId.class);

    // Details
    private String uid = SecureRandomTools.randomBase64String(10);
    private String usernameInContainer;

    public AttachableContainerUserToChangeId() {
    }

    @Override
    public void attachTo(AttachablePartContext context) {
        List<UnixUser> unixUsers = context.getServices().getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(this, LinkTypeConstants.USES, UnixUser.class);
        if (unixUsers.size() != 1) {
            logger.info("Must have a single unix user. Got {}. Skipping", unixUsers.size());
            return;
        }

        UnixUser unixUser = unixUsers.get(0);
        Long unixUserId = unixUser.getId();
        logger.info("Container user to change id: {} -> {}", usernameInContainer, unixUserId);
        context.getApplicationDefinition().addContainerUserToChangeId(usernameInContainer, unixUserId);
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.INFRASTRUCTURE;
    }

    @Override
    public String getResourceDescription() {
        return uid + " : " + usernameInContainer;
    }

    @Override
    public String getResourceName() {
        return usernameInContainer;
    }

    public String getUid() {
        return uid;
    }

    public String getUsernameInContainer() {
        return usernameInContainer;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUsernameInContainer(String usernameInContainer) {
        this.usernameInContainer = usernameInContainer;
    }

}
