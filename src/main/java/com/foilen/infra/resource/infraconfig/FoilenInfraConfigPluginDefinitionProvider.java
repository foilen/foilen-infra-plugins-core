/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.infraconfig;

import java.util.Arrays;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenInfraConfigPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinitionV1 = new IPPluginDefinitionV1("Foilen", "Infra Config", "To manage infra configuration", version);

        pluginDefinitionV1.addCustomResource(InfraConfig.class, InfraConfig.RESOURCE_TYPE, //
                Arrays.asList(), //
                Arrays.asList());
        pluginDefinitionV1.addCustomResource(InfraConfigPlugin.class, InfraConfigPlugin.RESOURCE_TYPE, //
                Arrays.asList(InfraConfigPlugin.PROPERTY_URL), //
                Arrays.asList());

        // Resource editors
        pluginDefinitionV1.addTranslations("/com/foilen/infra/resource/infraconfig/messages");
        pluginDefinitionV1.addResourceEditor(new InfraConfigEditor(), InfraConfigEditor.EDITOR_NAME);
        pluginDefinitionV1.addResourceEditor(new InfraConfigPluginEditor(), InfraConfigPluginEditor.EDITOR_NAME);

        // Updater Handler
        pluginDefinitionV1.addUpdateHandler(new InfraConfigUpdateHandler());

        return pluginDefinitionV1;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext) {
    }

}
