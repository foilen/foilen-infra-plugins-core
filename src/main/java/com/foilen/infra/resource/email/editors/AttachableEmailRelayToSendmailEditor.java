/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.editors;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.email.resources.AttachableEmailRelayToSendmail;
import com.foilen.infra.resource.email.resources.EmailRelay;

public class AttachableEmailRelayToSendmailEditor extends SimpleResourceEditor<AttachableEmailRelayToSendmail> {

    public static final String EDITOR_NAME = "Attachable Email Relay Sendmail";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(AttachableEmailRelayToSendmail.PROPERTY_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

        simpleResourceEditorDefinition.addResource("emailRelay", LinkTypeConstants.POINTS_TO, EmailRelay.class);

    }

    @Override
    public Class<AttachableEmailRelayToSendmail> getForResourceType() {
        return AttachableEmailRelayToSendmail.class;
    }

}
