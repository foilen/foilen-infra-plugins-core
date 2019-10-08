/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.plugin;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.composableapplication.ComposableApplication;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;

public class LetsEncryptWithFileAttachableEditor extends SimpleResourceEditor<LetsEncryptWithFileAttachable> {

    public static final String EDITOR_NAME = "Let's Encrypt WithFileAttachableEditor";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {
        simpleResourceEditorDefinition.addInputText(LetsEncryptWithFileAttachable.PROPERTY_NAME, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::nullIfEmpty);
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(LetsEncryptWithFileAttachable.PROPERTY_BASE_PATH, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::nullIfEmpty);
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
        });

        simpleResourceEditorDefinition.addResource("websiteCertificate", LinkTypeConstants.USES, WebsiteCertificate.class);
        simpleResourceEditorDefinition.addReverseResources("attachedTo", IPResource.class, ComposableApplication.LINK_TYPE_ATTACHED);
    }

    @Override
    public Class<LetsEncryptWithFileAttachable> getForResourceType() {
        return LetsEncryptWithFileAttachable.class;
    }

}
