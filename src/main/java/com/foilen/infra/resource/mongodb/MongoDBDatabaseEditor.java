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

public class MongoDBDatabaseEditor extends SimpleResourceEditor<MongoDBDatabase> {

    public static final String EDITOR_NAME = "MongoDB Database";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(MongoDBDatabase.PROPERTY_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addFormator(CommonFormatting::toLowerCase);
            fieldConfigConsumer.addValidator(CommonValidation::validateAlphaNumLower);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(MongoDBDatabase.PROPERTY_DESCRIPTION, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
        });

        simpleResourceEditorDefinition.addResources("mongodbServers", LinkTypeConstants.INSTALLED_ON, MongoDBServer.class);

        simpleResourceEditorDefinition.addReverseResources("usersAdmin", MongoDBUser.class, MongoDBUser.LINK_TYPE_ADMIN);
        simpleResourceEditorDefinition.addReverseResources("usersRead", MongoDBUser.class, MongoDBUser.LINK_TYPE_READ);
        simpleResourceEditorDefinition.addReverseResources("usersWrite", MongoDBUser.class, MongoDBUser.LINK_TYPE_WRITE);

    }

    @Override
    public Class<MongoDBDatabase> getForResourceType() {
        return MongoDBDatabase.class;
    }

}
