/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

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
import com.foilen.infra.resource.global.upgrader.V_2019070501_ApachePhp_EmailSender;
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

        // Change Handler
        pluginDefinition.addChangesHandler(new ManagedResourcesChangesEventHandler());

        return pluginDefinition;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext, InternalServicesContext internalServicesContext) {

        PluginUpgraderUtils.upgrade(commonServicesContext, internalServicesContext, getIPPluginDefinition(), //
                new V_2019061201_Refresh(), //
                new V_2019070501_ApachePhp_EmailSender() //
        );

    }

}
