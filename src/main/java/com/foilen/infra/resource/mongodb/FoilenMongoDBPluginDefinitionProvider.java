/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mongodb;

import java.util.Arrays;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenMongoDBPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinitionV1 = new IPPluginDefinitionV1("Foilen", "MongoDB", "To manage MongoDB databases", version);

        pluginDefinitionV1.addCustomResource(MongoDBServer.class, MongoDBServer.RESOURCE_TYPE, //
                Arrays.asList(MongoDBServer.PROPERTY_NAME), //
                Arrays.asList( //
                        MongoDBServer.PROPERTY_NAME, //
                        MongoDBServer.PROPERTY_DESCRIPTION //
                ));

        pluginDefinitionV1.addCustomResource(MongoDBDatabase.class, MongoDBDatabase.RESOURCE_TYPE, //
                Arrays.asList(MongoDBDatabase.PROPERTY_NAME), //
                Arrays.asList( //
                        MongoDBDatabase.PROPERTY_NAME, //
                        MongoDBDatabase.PROPERTY_DESCRIPTION //
                ));

        pluginDefinitionV1.addCustomResource(MongoDBUser.class, MongoDBUser.RESOURCE_TYPE, //
                Arrays.asList(MongoDBUser.PROPERTY_NAME), //
                Arrays.asList( //
                        MongoDBUser.PROPERTY_NAME, //
                        MongoDBUser.PROPERTY_DESCRIPTION //
                ));

        // Resource editors
        pluginDefinitionV1.addTranslations("/com/foilen/infra/resource/mongodb/messages");
        pluginDefinitionV1.addResourceEditor(new MongoDBServerEditor(), MongoDBServerEditor.EDITOR_NAME);
        pluginDefinitionV1.addResourceEditor(new MongoDBDatabaseEditor(), MongoDBDatabaseEditor.EDITOR_NAME);
        pluginDefinitionV1.addResourceEditor(new MongoDBUserEditor(), MongoDBUserEditor.EDITOR_NAME);

        // Change Handler
        pluginDefinitionV1.addChangesHandler(new MongoDBChangesEventHandler());

        return pluginDefinitionV1;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext, InternalServicesContext internalServicesContext) {
    }

}
