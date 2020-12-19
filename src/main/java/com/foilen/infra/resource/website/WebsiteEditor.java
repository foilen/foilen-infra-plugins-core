/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.website;

import java.util.Arrays;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.StringTools;

public class WebsiteEditor extends SimpleResourceEditor<Website> {

    public static final String EDITOR_NAME = "Website";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {
        simpleResourceEditorDefinition.addInputText(Website.PROPERTY_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

        simpleResourceEditorDefinition.addListInputText(Website.PROPERTY_DOMAIN_NAMES, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::toLowerCase);
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfigConsumer.addValidator(CommonValidation::validateDomainName);
        });

        simpleResourceEditorDefinition.addSelectOptionsField(Website.PROPERTY_IS_HTTPS, Arrays.asList("TRUE", "FALSE"), fieldConfig -> {
            fieldConfig.setConvertFromString(text -> StringTools.safeEquals("TRUE", text));
            fieldConfig.setConvertToString(value -> ((boolean) value) ? "TRUE" : "FALSE");
        });
        simpleResourceEditorDefinition.addSelectOptionsField(Website.PROPERTY_IS_HTTPS_ORIGIN_TO_HTTP, Arrays.asList("TRUE", "FALSE"), fieldConfig -> {
            fieldConfig.setConvertFromString(text -> StringTools.safeEquals("TRUE", text));
            fieldConfig.setConvertToString(value -> ((boolean) value) ? "TRUE" : "FALSE");
        });

        simpleResourceEditorDefinition.addSelectOptionsField(Website.PROPERTY_APPLICATION_ENDPOINT, DockerContainerEndpoints.allValues, fieldConfigConsumer -> {
        });

        simpleResourceEditorDefinition.addResources("applications", LinkTypeConstants.POINTS_TO, Application.class);
        simpleResourceEditorDefinition.addResource("websiteCertificate", LinkTypeConstants.USES, WebsiteCertificate.class);
        simpleResourceEditorDefinition.addResources("machines", LinkTypeConstants.INSTALLED_ON, Machine.class);
        simpleResourceEditorDefinition.addResources("machinesOnNoDns", Website.LINK_TYPE_INSTALLED_ON_NO_DNS, Machine.class);
    }

    @Override
    public Class<Website> getForResourceType() {
        return Website.class;
    }

}
