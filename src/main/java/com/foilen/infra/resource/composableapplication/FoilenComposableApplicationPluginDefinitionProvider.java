/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication;

import java.util.Arrays;
import java.util.Collections;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.infra.resource.composableapplication.parts.AttachableContainerUserToChangeId;
import com.foilen.infra.resource.composableapplication.parts.AttachableContainerUserToChangeIdEditor;
import com.foilen.infra.resource.composableapplication.parts.AttachableCronJob;
import com.foilen.infra.resource.composableapplication.parts.AttachableCronJobEditor;
import com.foilen.infra.resource.composableapplication.parts.AttachableMariaDB;
import com.foilen.infra.resource.composableapplication.parts.AttachableMariaDBEditor;
import com.foilen.infra.resource.composableapplication.parts.AttachableMongoDB;
import com.foilen.infra.resource.composableapplication.parts.AttachableMongoDBEditor;
import com.foilen.infra.resource.composableapplication.parts.AttachablePostgreSql;
import com.foilen.infra.resource.composableapplication.parts.AttachablePostgreSqlEditor;
import com.foilen.infra.resource.composableapplication.parts.AttachableService;
import com.foilen.infra.resource.composableapplication.parts.AttachableServiceEditor;
import com.foilen.infra.resource.composableapplication.parts.AttachableVolume;
import com.foilen.infra.resource.composableapplication.parts.AttachableVolumeEditor;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenComposableApplicationPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinition = new IPPluginDefinitionV1("Foilen", "Application - Composable", "A way to create application by attaching parts", version);

        pluginDefinition.addCustomResource(ComposableApplication.class, ComposableApplication.RESOURCE_TYPE, //
                Arrays.asList(ComposableApplication.PROPERTY_NAME), //
                Collections.emptyList());
        pluginDefinition.addCustomResource(AttachableContainerUserToChangeId.class, AttachableContainerUserToChangeId.RESOURCE_TYPE, //
                Arrays.asList(AttachableContainerUserToChangeId.PROPERTY_UID), //
                Arrays.asList(AttachableContainerUserToChangeId.PROPERTY_USERNAME_IN_CONTAINER));
        pluginDefinition.addCustomResource(AttachableCronJob.class, AttachableCronJob.RESOURCE_TYPE, //
                Arrays.asList(AttachableCronJob.PROPERTY_NAME), //
                Collections.emptyList());
        pluginDefinition.addCustomResource(AttachableMariaDB.class, AttachableMariaDB.RESOURCE_TYPE, //
                Arrays.asList(AttachableMariaDB.PROPERTY_NAME), //
                Collections.emptyList());
        pluginDefinition.addCustomResource(AttachableMongoDB.class, AttachableMongoDB.RESOURCE_TYPE, //
                Arrays.asList(AttachableMongoDB.PROPERTY_NAME), //
                Collections.emptyList());
        pluginDefinition.addCustomResource(AttachablePostgreSql.class, AttachablePostgreSql.RESOURCE_TYPE, //
                Arrays.asList(AttachablePostgreSql.PROPERTY_NAME), //
                Collections.emptyList());
        pluginDefinition.addCustomResource(AttachableService.class, AttachableService.RESOURCE_TYPE, //
                Arrays.asList(AttachableService.PROPERTY_NAME), //
                Collections.emptyList());
        pluginDefinition.addCustomResource(AttachableVolume.class, AttachableVolume.RESOURCE_TYPE, true, //
                Arrays.asList(AttachableVolume.PROPERTY_UID), //
                Arrays.asList(AttachableVolume.PROPERTY_HOST_FOLDER, AttachableVolume.PROPERTY_CONTAINER_FOLDER) //
        );

        // Resource editors
        pluginDefinition.addTranslations("/com/foilen/infra/resource/composableapplication/messages");
        pluginDefinition.addResourceEditor(new ComposableApplicationEditor(), ComposableApplicationEditor.EDITOR_NAME);
        pluginDefinition.addResourceEditor(new AttachableContainerUserToChangeIdEditor(), AttachableContainerUserToChangeIdEditor.EDITOR_NAME);
        pluginDefinition.addResourceEditor(new AttachableCronJobEditor(), AttachableCronJobEditor.EDITOR_NAME);
        pluginDefinition.addResourceEditor(new AttachableMariaDBEditor(), AttachableMariaDBEditor.EDITOR_NAME);
        pluginDefinition.addResourceEditor(new AttachableMongoDBEditor(), AttachableMongoDBEditor.EDITOR_NAME);
        pluginDefinition.addResourceEditor(new AttachablePostgreSqlEditor(), AttachablePostgreSqlEditor.EDITOR_NAME);
        pluginDefinition.addResourceEditor(new AttachableServiceEditor(), AttachableServiceEditor.EDITOR_NAME);
        pluginDefinition.addResourceEditor(new AttachableVolumeEditor(), AttachableVolumeEditor.EDITOR_NAME);

        // Updater Handler
        pluginDefinition.addUpdateHandler(new ComposableApplicationEventHandler());

        return pluginDefinition;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext) {
    }

}
