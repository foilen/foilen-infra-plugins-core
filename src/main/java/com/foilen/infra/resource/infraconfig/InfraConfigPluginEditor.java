/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.infraconfig;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;

public class InfraConfigPluginEditor extends SimpleResourceEditor<InfraConfigPlugin> {

    public static final String EDITOR_NAME = "Infrastructure Config - Plugin";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(InfraConfigPlugin.PROPERTY_URL, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addValidator(CommonValidation::validateUrl);
        });

        simpleResourceEditorDefinition.addInputText(InfraConfigPlugin.PROPERTY_SHA512, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
        });

    }

    @Override
    public Class<InfraConfigPlugin> getForResourceType() {
        return InfraConfigPlugin.class;
    }

}
