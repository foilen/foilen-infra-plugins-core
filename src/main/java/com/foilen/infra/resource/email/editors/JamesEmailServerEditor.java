/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.editors;

import java.util.Arrays;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.composableapplication.parts.AttachableMariaDB;
import com.foilen.infra.resource.email.resources.EmailDomain;
import com.foilen.infra.resource.email.resources.EmailServer;
import com.foilen.infra.resource.email.resources.JamesEmailServer;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.mariadb.MariaDBDatabase;
import com.foilen.infra.resource.mariadb.MariaDBUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.StringTools;

public class JamesEmailServerEditor extends SimpleResourceEditor<JamesEmailServer> {
    public static final String EDITOR_NAME = "James Email Server";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(EmailServer.PROPERTY_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(JamesEmailServer.PROPERTY_VERSION, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(EmailServer.PROPERTY_POSTMASTER_EMAIL, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfigConsumer.addValidator(CommonValidation::validateEmail);
        });

        simpleResourceEditorDefinition.addSelectOptionsField(JamesEmailServer.PROPERTY_DISABLE_BOUNCE_NOTIFY_POSTMASTER, Arrays.asList("TRUE", "FALSE"), fieldConfig -> {
            fieldConfig.setConvertFromString(text -> StringTools.safeEquals("TRUE", text));
            fieldConfig.setConvertToString(value -> ((boolean) value) ? "TRUE" : "FALSE");
        });
        simpleResourceEditorDefinition.addSelectOptionsField(JamesEmailServer.PROPERTY_DISABLE_BOUNCE_NOTIFY_SENDER, Arrays.asList("TRUE", "FALSE"), fieldConfig -> {
            fieldConfig.setConvertFromString(text -> StringTools.safeEquals("TRUE", text));
            fieldConfig.setConvertToString(value -> ((boolean) value) ? "TRUE" : "FALSE");
        });
        simpleResourceEditorDefinition.addSelectOptionsField(JamesEmailServer.PROPERTY_DISABLE_RELAY_DENIED_NOTIFY_SENDER, Arrays.asList("TRUE", "FALSE"), fieldConfig -> {
            fieldConfig.setConvertFromString(text -> StringTools.safeEquals("TRUE", text));
            fieldConfig.setConvertToString(value -> ((boolean) value) ? "TRUE" : "FALSE");
        });
        simpleResourceEditorDefinition.addSelectOptionsField(JamesEmailServer.PROPERTY_ENABLE_DEBUG_LOGS, Arrays.asList("TRUE", "FALSE"), fieldConfig -> {
            fieldConfig.setConvertFromString(text -> StringTools.safeEquals("TRUE", text));
            fieldConfig.setConvertToString(value -> ((boolean) value) ? "TRUE" : "FALSE");
        });
        simpleResourceEditorDefinition.addSelectOptionsField(JamesEmailServer.PROPERTY_ENABLE_DEBUG_DUMP_MESSAGES_DETAILS, Arrays.asList("TRUE", "FALSE"), fieldConfig -> {
            fieldConfig.setConvertFromString(text -> StringTools.safeEquals("TRUE", text));
            fieldConfig.setConvertToString(value -> ((boolean) value) ? "TRUE" : "FALSE");
        });
        simpleResourceEditorDefinition.addSelectOptionsField(JamesEmailServer.PROPERTY_EXPOSE_PORTS, Arrays.asList("TRUE", "FALSE"), fieldConfig -> {
            fieldConfig.setConvertFromString(text -> StringTools.safeEquals("TRUE", text));
            fieldConfig.setConvertToString(value -> ((boolean) value) ? "TRUE" : "FALSE");
        });

        simpleResourceEditorDefinition.addResource("mariaDBServer", "ATTACHED", AttachableMariaDB.class);
        simpleResourceEditorDefinition.addResource("mariaDBDatabase", LinkTypeConstants.USES, MariaDBDatabase.class);
        simpleResourceEditorDefinition.addResource("mariaDBUser", LinkTypeConstants.USES, MariaDBUser.class);
        simpleResourceEditorDefinition.addResource("unixUser", LinkTypeConstants.RUN_AS, UnixUser.class);
        simpleResourceEditorDefinition.addResource("machines", LinkTypeConstants.INSTALLED_ON, Machine.class);

        simpleResourceEditorDefinition.addResource("certSmtp", "USES_SMTP", WebsiteCertificate.class);
        simpleResourceEditorDefinition.addResource("certImap", "USES_IMAP", WebsiteCertificate.class);
        simpleResourceEditorDefinition.addResource("certPop3", "USES_POP3", WebsiteCertificate.class);

        simpleResourceEditorDefinition.addReverseResources("domains", EmailDomain.class, LinkTypeConstants.INSTALLED_ON);

    }

    @Override
    public Class<JamesEmailServer> getForResourceType() {
        return JamesEmailServer.class;
    }

}
