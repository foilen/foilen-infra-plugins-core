/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mongodb;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;

public class MongoDBServerEditor extends SimpleResourceEditor<MongoDBServer> {

    public static final String EDITOR_NAME = "MongoDB Server";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(MongoDBServer.PROPERTY_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addFormator(CommonFormatting::toLowerCase);
            fieldConfigConsumer.addValidator(CommonValidation::validateAlphaNumLower);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(MongoDBServer.PROPERTY_DESCRIPTION, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
        });
        simpleResourceEditorDefinition.addInputText(MongoDBServer.PROPERTY_VERSION, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(MongoDBServer.PROPERTY_ROOT_PASSWORD, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
        });

        simpleResourceEditorDefinition.addResource("unixUser", LinkTypeConstants.RUN_AS, UnixUser.class);
        simpleResourceEditorDefinition.addResource("machine", LinkTypeConstants.INSTALLED_ON, Machine.class);

        simpleResourceEditorDefinition.addReverseResources("databases", MongoDBDatabase.class, LinkTypeConstants.INSTALLED_ON);

    }

    @Override
    public Class<MongoDBServer> getForResourceType() {
        return MongoDBServer.class;
    }

}
