/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.infraconfig;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.mariadb.MariaDBDatabase;
import com.foilen.infra.resource.mariadb.MariaDBServer;
import com.foilen.infra.resource.mariadb.MariaDBUser;
import com.foilen.infra.resource.mongodb.MongoDBDatabase;
import com.foilen.infra.resource.mongodb.MongoDBServer;
import com.foilen.infra.resource.mongodb.MongoDBUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;

public class InfraConfigEditor extends SimpleResourceEditor<InfraConfig> {

    public static final String EDITOR_NAME = "Infrastructure Config";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_APPLICATION_ID, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addValidator(CommonValidation::validateAlphaNumLowerAndUpper);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_MAIL_HOST, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_MAIL_PORT, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_MAIL_USERNAME, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_MAIL_PASSWORD, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
        });

        // Login
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_LOGIN_DOMAIN_NAME, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(CommonFormatting::toLowerCase);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addValidator(CommonValidation::validateDomainName);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_LOGIN_EMAIL_FROM, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addValidator(CommonValidation::validateEmail);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_LOGIN_ADMINISTRATOR_EMAIL, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addValidator(CommonValidation::validateEmail);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_LOGIN_CSRF_SALT, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_LOGIN_COOKIE_SIGNATURE_SALT, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_LOGIN_VERSION, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

        simpleResourceEditorDefinition.addResource("loginWebsiteCertificate", InfraConfig.LINK_TYPE_LOGIN_USES, WebsiteCertificate.class);
        simpleResourceEditorDefinition.addResources("loginMachines", InfraConfig.LINK_TYPE_LOGIN_INSTALLED_ON, Machine.class);
        simpleResourceEditorDefinition.addResource("loginMariadbServer", InfraConfig.LINK_TYPE_LOGIN_USES, MariaDBServer.class);
        simpleResourceEditorDefinition.addResource("loginMariadbDatabase", InfraConfig.LINK_TYPE_LOGIN_USES, MariaDBDatabase.class);
        simpleResourceEditorDefinition.addResource("loginMariadbUser", InfraConfig.LINK_TYPE_LOGIN_USES, MariaDBUser.class);
        simpleResourceEditorDefinition.addResource("loginUnixUser", InfraConfig.LINK_TYPE_LOGIN_USES, UnixUser.class);

        // UI
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_UI_DOMAIN_NAME, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(CommonFormatting::toLowerCase);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addValidator(CommonValidation::validateDomainName);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_UI_EMAIL_FROM, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addValidator(CommonValidation::validateEmail);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_UI_ALERTS_TO_EMAIL, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addValidator(CommonValidation::validateEmail);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_UI_CSRF_SALT, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_UI_LOGIN_COOKIE_SIGNATURE_SALT, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_UI_VERSION, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_UI_DEBUG, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(value -> "true".equalsIgnoreCase(value) ? "true" : "false");
        });
        simpleResourceEditorDefinition.addInputText(InfraConfig.PROPERTY_UI_INFINITE_LOOP_TIMEOUT_IN_MS, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
        });

        simpleResourceEditorDefinition.addResource("uiWebsiteCertificate", InfraConfig.LINK_TYPE_UI_USES, WebsiteCertificate.class);
        simpleResourceEditorDefinition.addResources("uiMachines", InfraConfig.LINK_TYPE_UI_INSTALLED_ON, Machine.class);
        simpleResourceEditorDefinition.addResource("uiMariadbServer", InfraConfig.LINK_TYPE_UI_USES, MariaDBServer.class);
        simpleResourceEditorDefinition.addResource("uiMariadbDatabase", InfraConfig.LINK_TYPE_UI_USES, MariaDBDatabase.class);
        simpleResourceEditorDefinition.addResource("uiMariadbUser", InfraConfig.LINK_TYPE_UI_USES, MariaDBUser.class);
        simpleResourceEditorDefinition.addResource("uiMongodbServer", InfraConfig.LINK_TYPE_UI_USES, MongoDBServer.class);
        simpleResourceEditorDefinition.addResource("uiMongodbDatabase", InfraConfig.LINK_TYPE_UI_USES, MongoDBDatabase.class);
        simpleResourceEditorDefinition.addResource("uiMongodbUser", InfraConfig.LINK_TYPE_UI_USES, MongoDBUser.class);
        simpleResourceEditorDefinition.addResource("uiUnixUser", InfraConfig.LINK_TYPE_UI_USES, UnixUser.class);
        simpleResourceEditorDefinition.addResources("uiPlugins", InfraConfig.LINK_TYPE_UI_USES, InfraConfigPlugin.class);

    }

    @Override
    public Class<InfraConfig> getForResourceType() {
        return InfraConfig.class;
    }

}
