/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.editors;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.email.resources.EmailAccount;
import com.foilen.infra.resource.email.resources.EmailDomain;
import com.foilen.infra.resource.email.resources.EmailRedirection;
import com.foilen.infra.resource.email.resources.EmailRelay;
import com.foilen.infra.resource.email.resources.EmailServer;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;

public class EmailDomainEditor extends SimpleResourceEditor<EmailDomain> {

    public static final String EDITOR_NAME = "Email Domain";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(EmailDomain.PROPERTY_DOMAIN_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateDomainName);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(EmailDomain.PROPERTY_MX_DOMAIN_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateDomainName);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(EmailDomain.PROPERTY_IMAP_DOMAIN_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateDomainName);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(EmailDomain.PROPERTY_POP3_DOMAIN_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateDomainName);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

        simpleResourceEditorDefinition.addResource("emailServer", LinkTypeConstants.INSTALLED_ON, EmailServer.class);
        simpleResourceEditorDefinition.addResource("smtpCert", "USES_SMTP", WebsiteCertificate.class);
        simpleResourceEditorDefinition.addResource("imapCert", "USES_IMAP", WebsiteCertificate.class);
        simpleResourceEditorDefinition.addResource("pop3Cert", "USES_POP3", WebsiteCertificate.class);
        simpleResourceEditorDefinition.addResource("emailRelay", "SEND_THROUGHT", EmailRelay.class);

        simpleResourceEditorDefinition.addReverseResources("accounts", EmailAccount.class, LinkTypeConstants.INSTALLED_ON);
        simpleResourceEditorDefinition.addReverseResources("redirections", EmailRedirection.class, LinkTypeConstants.INSTALLED_ON);

    }

    @Override
    public Class<EmailDomain> getForResourceType() {
        return EmailDomain.class;
    }

}
