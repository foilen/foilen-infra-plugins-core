/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.global;

import java.util.Arrays;
import java.util.Collections;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.infra.resource.global.upgrader.V_2019061201_Refresh;
import com.foilen.infra.resource.global.upgrader.V_2019072401_Refresh_Apache;
import com.foilen.infra.resource.global.upgrader.V_2019090401_Refresh_InfraConfig;
import com.foilen.infra.resource.global.upgrader.V_2019111401_Refresh_ApachePhp;
import com.foilen.infra.resource.global.upgrader.V_2019123001_Refresh_ApacheJames;
import com.foilen.infra.resource.global.upgrader.V_2020011401_Refresh_All_Resources_With_Domains;
import com.foilen.infra.resource.global.upgrader.V_2020071401_ApachePhp_Set_Memory_Limit;
import com.foilen.infra.resource.global.upgrader.V_2020111101_MariaDB_GenerateUids;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenGlobalPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinition = new IPPluginDefinitionV1("Foilen", "Global", "Some global handlers", version);

        pluginDefinition.addCustomResource(UpgraderItem.class, UpgraderItem.RESOURCE_TYPE, //
                Arrays.asList(UpgraderItem.PROPERTY_NAME), //
                Collections.emptyList());

        pluginDefinition.addTranslations("/com/foilen/infra/resource/global/messages");
        pluginDefinition.addResourceEditor(new UpgraderItemEditor(), UpgraderItemEditor.EDITOR_NAME);

        // Change Handler
        pluginDefinition.addChangesHandler(new ManagedResourcesChangesEventHandler());

        return pluginDefinition;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext, InternalServicesContext internalServicesContext) {

        PluginUpgraderUtils.upgrade(commonServicesContext, internalServicesContext, getIPPluginDefinition(), //
                new V_2019061201_Refresh(), //
                new V_2019072401_Refresh_Apache(), //
                new V_2019090401_Refresh_InfraConfig(), //
                new V_2019111401_Refresh_ApachePhp(), //
                new V_2019123001_Refresh_ApacheJames(), //
                new V_2020011401_Refresh_All_Resources_With_Domains(), //
                new V_2020071401_ApachePhp_Set_Memory_Limit(), //
                new V_2020111101_MariaDB_GenerateUids() //
        );

    }

}
