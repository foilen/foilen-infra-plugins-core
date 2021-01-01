/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.service.TranslationService;
import com.foilen.infra.plugin.v1.core.visual.PageDefinition;
import com.foilen.infra.plugin.v1.core.visual.editor.ResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonPageItem;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.core.visual.pageItem.field.InputTextFieldPageItem;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tuple.Tuple2;

public class AttachableServiceEditor implements ResourceEditor<AttachableService> {

    public static final String EDITOR_NAME = "Attachable Service";

    @Override
    public void fillResource(CommonServicesContext servicesCtx, ChangesContext changesContext, Map<String, String> validFormValues, AttachableService editedResource) {
        editedResource.setName(validFormValues.get(AttachableService.PROPERTY_NAME));
        editedResource.setCommand(validFormValues.get(AttachableService.PROPERTY_COMMAND));
        editedResource.setWorkingDirectory(validFormValues.get(AttachableService.PROPERTY_WORKING_DIRECTORY));

        CommonResourceLink.fillResourceLink(servicesCtx, editedResource, LinkTypeConstants.RUN_AS, UnixUser.class, "unixUser", validFormValues, changesContext);
    }

    @Override
    public void formatForm(CommonServicesContext servicesCtx, Map<String, String> rawFormValues) {
        CommonFormatting.toLowerCase(rawFormValues, AttachableService.PROPERTY_NAME);
        CommonFormatting.trimSpaces(rawFormValues, AttachableService.PROPERTY_NAME, AttachableService.PROPERTY_WORKING_DIRECTORY);
        CommonFormatting.trimSpacesAround(rawFormValues, AttachableService.PROPERTY_COMMAND);
    }

    @Override
    public Class<AttachableService> getForResourceType() {
        return AttachableService.class;
    }

    @Override
    public PageDefinition providePageDefinition(CommonServicesContext servicesCtx, AttachableService editedResource) {

        TranslationService translationService = servicesCtx.getTranslationService();
        PageDefinition pageDefinition = new PageDefinition(translationService.translate("AttachableServiceEditor.title"));

        InputTextFieldPageItem nameField = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "AttachableServiceEditor.name", AttachableService.PROPERTY_NAME);
        InputTextFieldPageItem commandField = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "AttachableServiceEditor.command", AttachableService.PROPERTY_COMMAND);
        InputTextFieldPageItem workingDirectoryField = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "AttachableServiceEditor.workingDirectory",
                AttachableService.PROPERTY_WORKING_DIRECTORY);

        CommonResourceLink.addResourcePageItem(servicesCtx, pageDefinition, editedResource, LinkTypeConstants.RUN_AS, UnixUser.class, "AttachableServiceEditor.unixUser", "unixUser");

        if (editedResource != null) {
            nameField.setFieldValue(editedResource.getName());
            commandField.setFieldValue(editedResource.getCommand());
            workingDirectoryField.setFieldValue(editedResource.getWorkingDirectory());
        }

        return pageDefinition;
    }

    @Override
    public List<Tuple2<String, String>> validateForm(CommonServicesContext servicesCtx, Map<String, String> rawFormValues) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        errors.addAll(CommonValidation.validateNotNullOrEmpty(rawFormValues, AttachableService.PROPERTY_NAME));
        errors.addAll(CommonValidation.validateAlphaNumLower(rawFormValues, AttachableService.PROPERTY_NAME));
        return errors;
    }

}
