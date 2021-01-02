/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.editors;

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
import com.foilen.infra.resource.email.resources.EmailAccount;
import com.foilen.infra.resource.email.resources.EmailDomain;
import com.foilen.smalltools.hash.HashSha512;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Strings;

public class EmailAccountEditor implements ResourceEditor<EmailAccount> {

    public static final String EDITOR_NAME = "Email Account";

    protected static final String FIELD_PASSWORD_CONF = "passwordConf";
    protected static final String FIELD_PASSWORD = "password";

    protected static final String FIELD_DOMAIN = "domain";

    @Override
    public void fillResource(CommonServicesContext servicesCtx, ChangesContext changesContext, Map<String, String> validFormValues, EmailAccount resource) {

        resource.setAccountName(validFormValues.get(EmailAccount.PROPERTY_ACCOUNT_NAME));

        CommonResourceLink.fillResourcesLink(servicesCtx, resource, LinkTypeConstants.INSTALLED_ON, EmailDomain.class, FIELD_DOMAIN, validFormValues, changesContext);

        // Update password
        String password = validFormValues.get(FIELD_PASSWORD);
        if (!Strings.isNullOrEmpty(password)) {
            resource.setSha512Password(HashSha512.hashString(password));
        }

    }

    @Override
    public void formatForm(CommonServicesContext servicesCtx, Map<String, String> rawFormValues) {
        CommonFormatting.trimSpacesAround(rawFormValues, EmailAccount.PROPERTY_ACCOUNT_NAME);
        CommonFormatting.toLowerCase(rawFormValues, EmailAccount.PROPERTY_ACCOUNT_NAME);
    }

    @Override
    public Class<EmailAccount> getForResourceType() {
        return EmailAccount.class;
    }

    @Override
    public PageDefinition providePageDefinition(CommonServicesContext servicesCtx, EmailAccount resource) {

        TranslationService translationService = servicesCtx.getTranslationService();

        PageDefinition pageDefinition = new PageDefinition(translationService.translate("EmailAccountEditor.title"));

        InputTextFieldPageItem accountNamePageItem = CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "EmailAccountEditor.accountName", EmailAccount.PROPERTY_ACCOUNT_NAME);
        CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "EmailAccountEditor.password", FIELD_PASSWORD).setPassword(true);
        CommonPageItem.createInputTextField(servicesCtx, pageDefinition, "EmailAccountEditor.passwordConf", FIELD_PASSWORD_CONF).setPassword(true);

        CommonResourceLink.addResourcesPageItem(servicesCtx, pageDefinition, resource, LinkTypeConstants.INSTALLED_ON, EmailDomain.class, "EmailAccountEditor.domain", FIELD_DOMAIN);

        if (resource != null) {
            accountNamePageItem.setFieldValue(resource.getAccountName());
        }

        return pageDefinition;

    }

    @Override
    public List<Tuple2<String, String>> validateForm(CommonServicesContext servicesCtx, Map<String, String> rawFormValues) {

        List<Tuple2<String, String>> errors = CommonValidation.validateNotNullOrEmpty(rawFormValues, EmailAccount.PROPERTY_ACCOUNT_NAME);

        // Password are confirmed
        String password = rawFormValues.get(FIELD_PASSWORD);
        if (!Strings.isNullOrEmpty(password)) {
            errors.addAll(CommonValidation.validateSamePassword(rawFormValues, FIELD_PASSWORD, FIELD_PASSWORD_CONF));
        }

        return errors;

    }
}
