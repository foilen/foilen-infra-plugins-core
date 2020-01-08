/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;

public class ApachePhpHtPasswdEditor extends SimpleResourceEditor<ApachePhpHtPasswd> {

    public static final String EDITOR_NAME = "Apache PHP HT Password";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(ApachePhpHtPasswd.PROPERTY_USER, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addValidator(CommonValidation::validateAlphaNumLowerAndUpper);
        });
        simpleResourceEditorDefinition.addInputText(ApachePhpHtPasswd.PROPERTY_PASSWORD, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

    }

    @Override
    public Class<ApachePhpHtPasswd> getForResourceType() {
        return ApachePhpHtPasswd.class;
    }

}
