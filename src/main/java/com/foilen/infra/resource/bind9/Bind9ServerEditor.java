/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.service.TranslationService;
import com.foilen.infra.plugin.v1.core.visual.PageDefinition;
import com.foilen.infra.plugin.v1.core.visual.editor.ResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFieldHelper;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonPageItem;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.core.visual.pageItem.field.InputTextFieldPageItem;
import com.foilen.infra.plugin.v1.core.visual.pageItem.field.ListInputTextFieldPageItem;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Strings;

public class Bind9ServerEditor implements ResourceEditor<Bind9Server> {

    public static final String EDITOR_NAME = "Bind9";

    @Override
    public void fillResource(CommonServicesContext servicesCtx, ChangesContext changesContext, Map<String, String> validFormValues, Bind9Server editedResource) {
        editedResource.setName(validFormValues.get(Bind9Server.PROPERTY_NAME));
        editedResource.setAdminEmail(validFormValues.get(Bind9Server.PROPERTY_ADMIN_EMAIL));
        editedResource.setPort(Integer.valueOf(validFormValues.get(Bind9Server.PROPERTY_PORT)));
        editedResource.setNsDomainNames(new TreeSet<>(CommonFieldHelper.fromFormListToSet(validFormValues, Bind9Server.PROPERTY_NS_DOMAIN_NAMES)));

        CommonResourceLink.fillResourceLink(servicesCtx, editedResource, LinkTypeConstants.RUN_AS, UnixUser.class, "unixUser", validFormValues, changesContext);
        CommonResourceLink.fillResourcesLink(servicesCtx, editedResource, LinkTypeConstants.INSTALLED_ON, Machine.class, "machines", validFormValues, changesContext);
    }

    @Override
    public void formatForm(CommonServicesContext servicesCtx, Map<String, String> rawFormValues) {
        CommonFormatting.toLowerCase(rawFormValues, Bind9Server.PROPERTY_NAME, Bind9Server.PROPERTY_ADMIN_EMAIL);
        CommonFormatting.trimSpaces(rawFormValues, Bind9Server.PROPERTY_NAME, Bind9Server.PROPERTY_ADMIN_EMAIL);
    }

    @Override
    public Class<Bind9Server> getForResourceType() {
        return Bind9Server.class;
    }

    @Override
    public PageDefinition providePageDefinition(CommonServicesContext servicesCtx, Bind9Server editedResource) {

        TranslationService translationService = servicesCtx.getTranslationService();
        PageDefinition pageDefinition = new PageDefinition(translationService.translate("Bind9ServerEditor.title"));

        InputTextFieldPageItem nameField = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "Bind9ServerEditor.name", Bind9Server.PROPERTY_NAME);
        InputTextFieldPageItem portField = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "Bind9ServerEditor.port", Bind9Server.PROPERTY_PORT);
        portField.setFieldValue("53");
        InputTextFieldPageItem adminEmailField = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "Bind9ServerEditor.adminEmail", Bind9Server.PROPERTY_ADMIN_EMAIL);

        ListInputTextFieldPageItem nsDomainNamesField = CommonPageItem.createListInputTextFieldPageItem(servicesCtx, pageDefinition, "Bind9ServerEditor.nsDomainNames",
                Bind9Server.PROPERTY_NS_DOMAIN_NAMES);

        CommonResourceLink.addResourcePageItem(servicesCtx, pageDefinition, editedResource, LinkTypeConstants.RUN_AS, UnixUser.class, "Bind9ServerEditor.unixUser", "unixUser");
        CommonResourceLink.addResourcesPageItem(servicesCtx, pageDefinition, editedResource, LinkTypeConstants.INSTALLED_ON, Machine.class, "Bind9ServerEditor.machines", "machines");

        if (editedResource != null) {
            nameField.setFieldValue(editedResource.getName());
            adminEmailField.setFieldValue(editedResource.getAdminEmail());
            portField.setFieldValue(String.valueOf(editedResource.getPort()));
            nsDomainNamesField.setFieldValues(CommonFieldHelper.fromSetToList(editedResource.getNsDomainNames()));
        }

        return pageDefinition;
    }

    @Override
    public List<Tuple2<String, String>> validateForm(CommonServicesContext servicesCtx, Map<String, String> rawFormValues) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        errors.addAll(
                CommonValidation.validateNotNullOrEmpty(rawFormValues, Bind9Server.PROPERTY_NAME, Bind9Server.PROPERTY_ADMIN_EMAIL, Bind9Server.PROPERTY_PORT, Bind9Server.PROPERTY_NS_DOMAIN_NAMES));
        errors.addAll(CommonValidation.validateAlphaNumLower(rawFormValues, Bind9Server.PROPERTY_NAME));
        errors.addAll(CommonValidation.validateDomainName(rawFormValues, Bind9Server.PROPERTY_NS_DOMAIN_NAMES));

        errors.addAll(CommonValidation.validateEmail(rawFormValues, Bind9Server.PROPERTY_ADMIN_EMAIL));

        // Port
        String portString = rawFormValues.get(Bind9Server.PROPERTY_PORT);
        if (!Strings.isNullOrEmpty(portString)) {
            try {
                Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                errors.add(new Tuple2<>(Bind9Server.PROPERTY_PORT, "error.notInteger"));
            }
        }

        return errors;
    }

}
