/*
    Foilen Infra Plugins Core Resources
    https://github.com/foilen/foilen-infra-plugins-core-resources
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugins.core.resources;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenCoreResourcesPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {
        // Get version
        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core-resources.txt");
        } catch (Exception e) {
        }
        IPPluginDefinitionV1 pluginDefinition = new IPPluginDefinitionV1("Foilen", "Core Resources", "Common resources for the system", version);

        // TODO ++++ Things
        // pluginDefinition.addCustomResource(Bind9Server.class, Bind9Server.RESOURCE_TYPE, //
        // Arrays.asList(Bind9Server.PROPERTY_NAME), //
        // Collections.emptyList());

        // Resource editors
        // pluginDefinition.addTranslations("/com/foilen/infra/resource/bind9/messages");
        // pluginDefinition.addResourceEditor(new Bind9ServerEditor(), Bind9ServerEditor.EDITOR_NAME);

        // Updater Handler
        // pluginDefinition.addUpdateHandler(new Bind9UpdateEventHandler());
        // pluginDefinition.addUpdateHandler(new Bind9DnsEntryUpdateEventHandler());

        return pluginDefinition;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext) {
    }

}
