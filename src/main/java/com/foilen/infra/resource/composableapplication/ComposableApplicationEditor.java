/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;

public class ComposableApplicationEditor extends SimpleResourceEditor<ComposableApplication> {

    public static final String EDITOR_NAME = "Composable Application";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {
        simpleResourceEditorDefinition.addInputText(ComposableApplication.PROPERTY_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addFormator(CommonFormatting::toLowerCase);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfigConsumer.addValidator(CommonValidation::validateAlphaNumLower);
        });
        simpleResourceEditorDefinition.addInputText(ComposableApplication.PROPERTY_FROM, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(ComposableApplication.PROPERTY_MAIN_COMMAND, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
        });
        simpleResourceEditorDefinition.addInputText(ComposableApplication.PROPERTY_MAIN_WORKING_DIRECTORY, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
        });

        simpleResourceEditorDefinition.addListInputText(ComposableApplication.PROPERTY_ENVIRONMENTS, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
        });
        simpleResourceEditorDefinition.addListInputText(ComposableApplication.PROPERTY_PORTS_EXPOSED_TCP, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
        });
        simpleResourceEditorDefinition.addListInputText(ComposableApplication.PROPERTY_PORTS_EXPOSED_UDP, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
        });
        simpleResourceEditorDefinition.addListInputText(ComposableApplication.PROPERTY_PORTS_ENDPOINT, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
        });

        simpleResourceEditorDefinition.addResource("unixUser", LinkTypeConstants.RUN_AS, UnixUser.class);
        simpleResourceEditorDefinition.addResources("machines", LinkTypeConstants.INSTALLED_ON, Machine.class);
        simpleResourceEditorDefinition.addResources("attachableParts", ComposableApplication.LINK_TYPE_ATTACHED, AttachablePart.class);

    }

    @Override
    public Class<ComposableApplication> getForResourceType() {
        return ComposableApplication.class;
    }

}
