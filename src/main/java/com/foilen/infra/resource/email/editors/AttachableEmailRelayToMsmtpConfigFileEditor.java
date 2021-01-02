/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.editors;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.email.resources.AttachableEmailRelayToMsmtpConfigFile;
import com.foilen.infra.resource.email.resources.EmailRelay;
import com.google.common.base.Strings;

public class AttachableEmailRelayToMsmtpConfigFileEditor extends SimpleResourceEditor<AttachableEmailRelayToMsmtpConfigFile> {

    public static final String EDITOR_NAME = "Attachable Email Relay to msmtp Config File";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(AttachableEmailRelayToMsmtpConfigFile.PROPERTY_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(AttachableEmailRelayToMsmtpConfigFile.PROPERTY_CONFIG_PATH, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addFormator(value -> Strings.isNullOrEmpty(value) ? "/etc/msmtprc" : value);
        });
        simpleResourceEditorDefinition.addInputText(AttachableEmailRelayToMsmtpConfigFile.PROPERTY_USE_TLS, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addFormator(value -> Strings.isNullOrEmpty(value) ? "false" : value);
        });

        simpleResourceEditorDefinition.addResource("emailRelay", LinkTypeConstants.POINTS_TO, EmailRelay.class);

    }

    @Override
    public Class<AttachableEmailRelayToMsmtpConfigFile> getForResourceType() {
        return AttachableEmailRelayToMsmtpConfigFile.class;
    }

}
