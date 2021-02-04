/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mariadb;

import java.util.Arrays;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenMariaDBPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinitionV1 = new IPPluginDefinitionV1("Foilen", "MariaDB", "To manage MariaDB databases", version);

        pluginDefinitionV1.addCustomResource(MariaDBServer.class, MariaDBServer.RESOURCE_TYPE, //
                Arrays.asList(MariaDBServer.PROPERTY_NAME));

        pluginDefinitionV1.addCustomResource(MariaDBDatabase.class, MariaDBDatabase.RESOURCE_TYPE, //
                Arrays.asList(MariaDBDatabase.PROPERTY_UID));

        pluginDefinitionV1.addCustomResource(MariaDBUser.class, MariaDBUser.RESOURCE_TYPE, //
                Arrays.asList(MariaDBUser.PROPERTY_UID));

        // Resource editors
        pluginDefinitionV1.addTranslations("/com/foilen/infra/resource/mariadb/messages");
        pluginDefinitionV1.addResourceEditor(new MariaDBDatabaseEditor(), MariaDBDatabaseEditor.EDITOR_NAME);
        pluginDefinitionV1.addResourceEditor(new MariaDBServerEditor(), MariaDBServerEditor.EDITOR_NAME);
        pluginDefinitionV1.addResourceEditor(new MariaDBUserEditor(), MariaDBUserEditor.EDITOR_NAME);

        // Change Handler
        pluginDefinitionV1.addChangesHandler(new MariaDBChangesEventHandler());

        return pluginDefinitionV1;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext, InternalServicesContext internalServicesContext) {
    }

}
