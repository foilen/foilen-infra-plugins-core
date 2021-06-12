/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import java.util.List;
import java.util.Map;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.service.TranslationService;
import com.foilen.infra.plugin.v1.core.visual.PageDefinition;
import com.foilen.infra.plugin.v1.core.visual.editor.ResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonPageItem;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.core.visual.pageItem.field.InputTextFieldPageItem;
import com.foilen.infra.plugin.v1.core.visual.pageItem.field.SelectOptionsPageItem;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Strings;

public class ManualDnsEntryEditor implements ResourceEditor<DnsEntry> {

    public static final String EDITOR_NAME = "Manual DnsEntry";

    @Override
    public void fillResource(CommonServicesContext servicesCtx, ChangesContext changesContext, Map<String, String> validFormValues, DnsEntry resource) {
        resource.setName(validFormValues.get(DnsEntry.PROPERTY_NAME));
        resource.setType(DnsEntryType.valueOf(validFormValues.get(DnsEntry.PROPERTY_TYPE)));
        resource.setDetails(validFormValues.get(DnsEntry.PROPERTY_DETAILS));

        String priority = validFormValues.get(DnsEntry.PROPERTY_PRIORITY);
        if (Strings.isNullOrEmpty(priority)) {
            resource.setPriority(10);
        } else {
            resource.setPriority(Integer.valueOf(validFormValues.get(DnsEntry.PROPERTY_PRIORITY)));
        }
        String weight = validFormValues.get(DnsEntry.PROPERTY_WEIGHT);
        if (Strings.isNullOrEmpty(weight)) {
            resource.setWeight(1);
        } else {
            resource.setPriority(Integer.valueOf(validFormValues.get(DnsEntry.PROPERTY_WEIGHT)));
        }
        String port = validFormValues.get(DnsEntry.PROPERTY_PORT);
        if (Strings.isNullOrEmpty(port)) {
            resource.setPort(0);
        } else {
            resource.setPriority(Integer.valueOf(validFormValues.get(DnsEntry.PROPERTY_PORT)));
        }
    }

    @Override
    public void formatForm(CommonServicesContext servicesCtx, Map<String, String> rawFormValues) {
        CommonFormatting.trimSpacesAround(rawFormValues);
        CommonFormatting.toLowerCase(rawFormValues, DnsEntry.PROPERTY_NAME);
        CommonFormatting.toUpperCase(rawFormValues, DnsEntry.PROPERTY_TYPE);
    }

    @Override
    public Class<DnsEntry> getForResourceType() {
        return DnsEntry.class;
    }

    @Override
    public PageDefinition providePageDefinition(CommonServicesContext servicesCtx, DnsEntry resource) {
        TranslationService translationService = servicesCtx.getTranslationService();

        PageDefinition pageDefinition = new PageDefinition(translationService.translate("ManualDnsEntryEditor.title"));

        InputTextFieldPageItem namePageItem = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "ManualDnsEntryEditor.name", DnsEntry.PROPERTY_NAME);
        SelectOptionsPageItem typePageItem = CommonPageItem.createSelectOptionsField(servicesCtx, pageDefinition, "ManualDnsEntryEditor.type", DnsEntry.PROPERTY_TYPE, DnsEntryType.values());
        InputTextFieldPageItem detailsPageItem = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "ManualDnsEntryEditor.details", DnsEntry.PROPERTY_DETAILS);
        InputTextFieldPageItem priorityPageItem = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "ManualDnsEntryEditor.priority", DnsEntry.PROPERTY_PRIORITY);
        InputTextFieldPageItem weightPageItem = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "ManualDnsEntryEditor.weight", DnsEntry.PROPERTY_WEIGHT);
        InputTextFieldPageItem portPageItem = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "ManualDnsEntryEditor.port", DnsEntry.PROPERTY_PORT);

        if (resource != null) {
            namePageItem.setFieldValue(resource.getName());
            typePageItem.setFieldValue(resource.getType().name());
            detailsPageItem.setFieldValue(resource.getDetails());
            priorityPageItem.setFieldValue(String.valueOf(resource.getPriority()));
            weightPageItem.setFieldValue(String.valueOf(resource.getWeight()));
            portPageItem.setFieldValue(String.valueOf(resource.getPort()));
        }

        return pageDefinition;
    }

    @Override
    public List<Tuple2<String, String>> validateForm(CommonServicesContext servicesCtx, Map<String, String> rawFormValues) {
        List<Tuple2<String, String>> errors = CommonValidation.validateNotNullOrEmpty(rawFormValues, DnsEntry.PROPERTY_NAME, DnsEntry.PROPERTY_TYPE, DnsEntry.PROPERTY_DETAILS);
        errors.addAll(CommonValidation.validateDomainName(rawFormValues, DnsEntry.PROPERTY_NAME));
        errors.addAll(CommonValidation.validateInEnum(rawFormValues, DnsEntryType.values(), DnsEntry.PROPERTY_TYPE));
        try {
            if (DnsEntryType.valueOf(rawFormValues.get(DnsEntry.PROPERTY_TYPE)) == DnsEntryType.A) {
                errors.addAll(CommonValidation.validateIpAddress(rawFormValues, DnsEntry.PROPERTY_DETAILS));
            }
        } catch (Exception e) {
        }

        String priority = rawFormValues.get(DnsEntry.PROPERTY_PRIORITY);
        if (!Strings.isNullOrEmpty(priority)) {
            try {
                Integer.parseInt(priority);
            } catch (NumberFormatException e) {
                errors.add(new Tuple2<>(DnsEntry.PROPERTY_PRIORITY, "error.notInteger"));
            }
        }
        String weight = rawFormValues.get(DnsEntry.PROPERTY_WEIGHT);
        if (!Strings.isNullOrEmpty(weight)) {
            try {
                Integer.parseInt(weight);
            } catch (NumberFormatException e) {
                errors.add(new Tuple2<>(DnsEntry.PROPERTY_WEIGHT, "error.notInteger"));
            }
        }
        String port = rawFormValues.get(DnsEntry.PROPERTY_PORT);
        if (!Strings.isNullOrEmpty(port)) {
            try {
                Integer.parseInt(port);
            } catch (NumberFormatException e) {
                errors.add(new Tuple2<>(DnsEntry.PROPERTY_PORT, "error.notInteger"));
            }
        }
        return errors;
    }

}
