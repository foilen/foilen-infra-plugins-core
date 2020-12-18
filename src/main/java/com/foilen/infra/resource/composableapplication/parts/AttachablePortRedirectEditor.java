/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.parts;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;

public class AttachablePortRedirectEditor extends SimpleResourceEditor<AttachablePortRedirect> {

    public static final String EDITOR_NAME = "Attachable Port Redirect";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(AttachablePortRedirect.PROPERTY_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(AttachablePortRedirect.PROPERTY_LOCAL_PORT, fieldConfigConsumer -> {
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(AttachablePortRedirect.PROPERTY_TO_ENDPOINT, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

        simpleResourceEditorDefinition.addResource("application", LinkTypeConstants.POINTS_TO, Application.class);

    }

    @Override
    public Class<AttachablePortRedirect> getForResourceType() {
        return AttachablePortRedirect.class;
    }

}
