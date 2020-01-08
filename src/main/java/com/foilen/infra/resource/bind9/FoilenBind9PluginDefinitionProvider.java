/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9;

import java.util.Arrays;
import java.util.Collections;

import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenBind9PluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinition = new IPPluginDefinitionV1("Foilen", "DNS - Bind9", "A Bind9 DNS server that uses all the DnsEntries", version);

        pluginDefinition.addCustomResource(Bind9Server.class, Bind9Server.RESOURCE_TYPE, //
                Arrays.asList(Bind9Server.PROPERTY_NAME), //
                Collections.emptyList());

        // Resource editors
        pluginDefinition.addTranslations("/com/foilen/infra/resource/bind9/messages");
        pluginDefinition.addResourceEditor(new Bind9ServerEditor(), Bind9ServerEditor.EDITOR_NAME);

        // Change Handler
        pluginDefinition.addChangesHandler(new Bind9ChangesEventHandler());

        return pluginDefinition;
    }

}
