/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.unixuser;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.TranslationService;
import com.foilen.infra.plugin.v1.core.visual.PageDefinition;
import com.foilen.infra.plugin.v1.core.visual.editor.ResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonPageItem;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.core.visual.pageItem.field.InputTextFieldPageItem;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Strings;

public class SystemUnixUserEditor implements ResourceEditor<SystemUnixUser> {

    public static final String EDITOR_NAME = "System Unix User";

    @Override
    public void fillResource(CommonServicesContext servicesCtx, ChangesContext changesContext, Map<String, String> validFormValues, SystemUnixUser resource) {
        resource.setId(Long.valueOf(validFormValues.get(UnixUser.PROPERTY_ID)));
        resource.setName(validFormValues.get(UnixUser.PROPERTY_NAME));
    }

    @Override
    public void formatForm(CommonServicesContext servicesCtx, Map<String, String> rawFormValues) {
        CommonFormatting.trimSpacesAround(rawFormValues, UnixUser.PROPERTY_ID);
        CommonFormatting.trimSpacesAround(rawFormValues, UnixUser.PROPERTY_NAME);
        CommonFormatting.toLowerCase(rawFormValues, UnixUser.PROPERTY_NAME);
    }

    @Override
    public Class<SystemUnixUser> getForResourceType() {
        return SystemUnixUser.class;
    }

    @Override
    public PageDefinition providePageDefinition(CommonServicesContext servicesCtx, SystemUnixUser resource) {

        TranslationService translationService = servicesCtx.getTranslationService();

        PageDefinition pageDefinition = new PageDefinition(translationService.translate("SystemUnixUserEditor.title"));

        InputTextFieldPageItem idPageItem = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "SystemUnixUserEditor.id", UnixUser.PROPERTY_ID);
        InputTextFieldPageItem namePageItem = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "SystemUnixUserEditor.name", UnixUser.PROPERTY_NAME);

        if (resource != null) {
            idPageItem.setFieldValue(String.valueOf(resource.getId()));
            namePageItem.setFieldValue(resource.getName());
        }

        return pageDefinition;

    }

    @Override
    public List<Tuple2<String, String>> validateForm(CommonServicesContext servicesCtx, Map<String, String> rawFormValues) {

        List<Tuple2<String, String>> errors = CommonValidation.validateNotNullOrEmpty(rawFormValues, UnixUser.PROPERTY_NAME);
        if (errors.isEmpty()) {

            // If new name or changing name, make sure no collision
            IPResourceService resourceService = servicesCtx.getResourceService();
            String username = rawFormValues.get(UnixUser.PROPERTY_NAME);
            Optional<SystemUnixUser> unixUser = resourceService.resourceFind(resourceService.createResourceQuery(SystemUnixUser.class) //
                    .propertyEquals(UnixUser.PROPERTY_NAME, username));
            if (unixUser.isPresent()) {
                String expectedInternalId = null;
                try {
                    String idText = rawFormValues.get("_resourceId");
                    if (!Strings.isNullOrEmpty(idText)) {
                        expectedInternalId = idText;
                    }
                } catch (Exception e) {
                }

                if (!unixUser.get().getInternalId().equals(expectedInternalId)) {
                    errors.add(new Tuple2<>(UnixUser.PROPERTY_NAME, "error.nameTaken"));
                }
            }

        }

        errors.addAll(CommonValidation.validateNotNullOrEmpty(rawFormValues, UnixUser.PROPERTY_ID));
        if (!Strings.isNullOrEmpty(rawFormValues.get(UnixUser.PROPERTY_ID))) {
            try {
                Long id = Long.valueOf(rawFormValues.get(UnixUser.PROPERTY_ID));
                if (id >= 70000) {
                    errors.add(new Tuple2<String, String>(UnixUser.PROPERTY_ID, "SystemUnixUserEditor.error.idIsTooHigh"));
                }
            } catch (Exception e) {
                errors.add(new Tuple2<String, String>(UnixUser.PROPERTY_ID, "SystemUnixUserEditor.error.idMustBeNumeric"));
            }
        }

        return errors;

    }

}
