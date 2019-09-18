/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.usagemetrics.editors;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.email.resources.JamesEmailServer;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.mongodb.MongoDBServer;
import com.foilen.infra.resource.usagemetrics.resources.UsageMetricsConfig;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.google.common.base.Strings;

public class UsageMetricsConfigEditor extends SimpleResourceEditor<UsageMetricsConfig> {

    public static final String EDITOR_NAME = "Usage Metrics Config";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(UsageMetricsConfig.PROPERTY_VERSION, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(UsageMetricsConfig.PROPERTY_HOST_KEY_SALT, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addFormator(v -> Strings.isNullOrEmpty(v) ? SecureRandomTools.randomHexString(10) : v);
        });
        simpleResourceEditorDefinition.addInputText(UsageMetricsConfig.PROPERTY_MONGO_DATABASE, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(UsageMetricsConfig.PROPERTY_MONGO_USER, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(UsageMetricsConfig.PROPERTY_MONGO_PASSWORD, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addResource("mongoDbServer", LinkTypeConstants.USES, MongoDBServer.class);
        simpleResourceEditorDefinition.addResource("centralInstalledOn", LinkTypeConstants.INSTALLED_ON, Machine.class);
        simpleResourceEditorDefinition.addReverseResources("websitesFrom", Website.class, LinkTypeConstants.POINTS_TO);
        simpleResourceEditorDefinition.addResources("jamesEmailServers", LinkTypeConstants.USES, JamesEmailServer.class);

    }

    @Override
    public Class<UsageMetricsConfig> getForResourceType() {
        return UsageMetricsConfig.class;
    }

}
