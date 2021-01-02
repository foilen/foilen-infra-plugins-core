/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.domain;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;

public class DomainEditor extends SimpleResourceEditor<Domain> {

    public static final String EDITOR_NAME = "Domain";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(Domain.PROPERTY_NAME, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpaces);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addValidator(CommonValidation::validateDomainName);
        });

    }

    @Override
    public Class<Domain> getForResourceType() {
        return Domain.class;
    }

}
