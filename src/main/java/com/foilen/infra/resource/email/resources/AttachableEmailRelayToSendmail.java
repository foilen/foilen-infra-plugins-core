/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.resources;

import java.util.Optional;

import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.AttachablePartContext;
import com.foilen.infra.resource.composableapplication.ComposableApplication;
import com.foilen.infra.resource.utils.PostfixUtils;

/**
 * To configure and use sendmail as a relay on a {@link ComposableApplication} that redirects all mails to the {@link EmailRelay}.
 *
 * Link to:
 * <ul>
 * <li>{@link EmailRelay}: (1) POINTS_TO - Where to send the emails</li>
 * </ul>
 */
public class AttachableEmailRelayToSendmail extends AttachablePart {

    public static final String RESOURCE_TYPE = "Attachable Email Relay Sendmail";

    public static final String PROPERTY_NAME = "name";

    // Details
    private String name;

    @Override
    public void attachTo(AttachablePartContext context) {

        Optional<EmailRelay> emailRelayOptional = context.getServices().getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(this, LinkTypeConstants.POINTS_TO, EmailRelay.class)
                .stream().findAny();
        if (!emailRelayOptional.isPresent()) {
            return;
        }

        EmailRelay emailRelay = emailRelayOptional.get();
        PostfixUtils.addConfigAndServiceForRelay("localhost", emailRelay, context.getApplicationDefinition());

    }

    public String getName() {
        return name;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.EMAIL;
    }

    @Override
    public String getResourceDescription() {
        return "Relay";
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
