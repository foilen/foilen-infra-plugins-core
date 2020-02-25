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
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.core.visual.pageItem.LabelPageItem;
import com.foilen.infra.plugin.v1.core.visual.pageItem.field.InputTextFieldPageItem;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.helper.UnixUserAvailableIdHelper;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Strings;

public class UnixUserEditor implements ResourceEditor<UnixUser> {

    public static final String EDITOR_NAME = "Unix User";

    public static final String FIELD_PASSWORD_CONF = "passwordConf";
    public static final String FIELD_PASSWORD = "password";
    public static final String CLEAR_PASSWORD_CHAR = "*";

    public static final String LINK_INSTALLED_ON = "machines";

    @Override
    public void fillResource(CommonServicesContext servicesCtx, ChangesContext changesContext, Map<String, String> validFormValues, UnixUser resource) {
        if (resource.getId() == null) {
            // Choose id
            resource.setId(UnixUserAvailableIdHelper.getNextAvailableId());
        }

        // Other common properties
        resource.setName(validFormValues.get(UnixUser.PROPERTY_NAME));
        resource.setKeepClearPassword(Boolean.valueOf(validFormValues.get(UnixUser.PROPERTY_KEEP_CLEAR_PASSWORD)));

        // Update password
        String password = validFormValues.get(FIELD_PASSWORD);
        String passwordConf = validFormValues.get(FIELD_PASSWORD_CONF);
        if (Strings.isNullOrEmpty(passwordConf) && CLEAR_PASSWORD_CHAR.equals(password)) {
            // Clear the password
            resource.setPassword(null);
            resource.setHashedPassword(null);
        } else if (!Strings.isNullOrEmpty(password)) {
            // Set the password
            resource.setPassword(password);
        }

        // Update links
        CommonResourceLink.fillResourcesLink(servicesCtx, resource, LinkTypeConstants.INSTALLED_ON, Machine.class, LINK_INSTALLED_ON, validFormValues, changesContext);

    }

    @Override
    public void formatForm(CommonServicesContext servicesCtx, Map<String, String> rawFormValues) {
        CommonFormatting.trimSpacesAround(rawFormValues, UnixUser.PROPERTY_NAME);
        CommonFormatting.toLowerCase(rawFormValues, UnixUser.PROPERTY_NAME);

        // Keep clear password
        if (!StringTools.safeEquals(rawFormValues.get(UnixUser.PROPERTY_KEEP_CLEAR_PASSWORD), "true")) {
            rawFormValues.put(UnixUser.PROPERTY_KEEP_CLEAR_PASSWORD, "false");
        }

    }

    @Override
    public Class<UnixUser> getForResourceType() {
        return UnixUser.class;
    }

    @Override
    public PageDefinition providePageDefinition(CommonServicesContext servicesCtx, UnixUser resource) {

        TranslationService translationService = servicesCtx.getTranslationService();

        PageDefinition pageDefinition = new PageDefinition(translationService.translate("UnixUserEditor.title"));

        if (resource != null) {
            pageDefinition.addPageItem(new LabelPageItem().setText(translationService.translate("UnixUserEditor.id", resource.getId())));
        }
        InputTextFieldPageItem namePageItem = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "UnixUserEditor.name", UnixUser.PROPERTY_NAME);
        pageDefinition.addPageItem(new LabelPageItem().setText(translationService.translate("UnixUserEditor.clearPasswordInstructions")));
        CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "UnixUserEditor.password", FIELD_PASSWORD).setPassword(true);
        CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "UnixUserEditor.passwordConf", FIELD_PASSWORD_CONF).setPassword(true);

        InputTextFieldPageItem keepClearPassword = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "UnixUserEditor.keepClearPassword", UnixUser.PROPERTY_KEEP_CLEAR_PASSWORD);
        keepClearPassword.setFieldValue("false");

        if (resource != null) {
            namePageItem.setFieldValue(resource.getName());
            keepClearPassword.setFieldValue(String.valueOf(resource.isKeepClearPassword()));
        }

        CommonResourceLink.addResourcesPageItem(servicesCtx, pageDefinition, resource, LinkTypeConstants.INSTALLED_ON, Machine.class, "UnixUserEditor.machines", LINK_INSTALLED_ON);

        // Label: current password
        if (resource != null && resource.getPassword() != null) {
            pageDefinition.addPageItem(new LabelPageItem().setText(translationService.translate("UnixUserEditor.visiblePassword", resource.getPassword())));
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
            Optional<UnixUser> unixUser = resourceService.resourceFind(resourceService.createResourceQuery(UnixUser.class) //
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

        // Password are confirmed
        String password = rawFormValues.get(FIELD_PASSWORD);
        String passwordConf = rawFormValues.get(FIELD_PASSWORD_CONF);
        if (Strings.isNullOrEmpty(passwordConf) && CLEAR_PASSWORD_CHAR.equals(password)) {
            // Fine, will clear the password
        } else {
            errors.addAll(CommonValidation.validateSamePassword(rawFormValues, FIELD_PASSWORD, FIELD_PASSWORD_CONF));
        }

        return errors;

    }

}
