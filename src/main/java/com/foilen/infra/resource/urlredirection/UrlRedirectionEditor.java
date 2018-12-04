/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.urlredirection;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.google.common.base.Strings;

public class UrlRedirectionEditor extends SimpleResourceEditor<UrlRedirection> {

    public static final String EDITOR_NAME = "Url Redirection";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(UrlRedirection.PROPERTY_DOMAIN_NAME, fieldConfig -> {
            fieldConfig.addValidator(CommonValidation::validateDomainName);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
        });
        simpleResourceEditorDefinition.addInputText(UrlRedirection.PROPERTY_HTTP_REDIRECT_TO_URL, fieldConfig -> {
            fieldConfig.addValidator(CommonValidation::validateUrl);
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
        });
        simpleResourceEditorDefinition.addInputText(UrlRedirection.PROPERTY_HTTP_IS_PERMANENT, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(value -> Strings.isNullOrEmpty(value) ? "false" : value);
        });
        simpleResourceEditorDefinition.addInputText(UrlRedirection.PROPERTY_HTTPS_REDIRECT_TO_URL, fieldConfig -> {
            fieldConfig.addValidator(CommonValidation::validateUrl);
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
        });
        simpleResourceEditorDefinition.addInputText(UrlRedirection.PROPERTY_HTTPS_IS_PERMANENT, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(value -> Strings.isNullOrEmpty(value) ? "false" : value);
        });

        simpleResourceEditorDefinition.addResources("machines", LinkTypeConstants.INSTALLED_ON, Machine.class);
        simpleResourceEditorDefinition.addResource("websiteCertificate", LinkTypeConstants.USES, WebsiteCertificate.class);

    }

    @Override
    public Class<UrlRedirection> getForResourceType() {
        return UrlRedirection.class;
    }

}
