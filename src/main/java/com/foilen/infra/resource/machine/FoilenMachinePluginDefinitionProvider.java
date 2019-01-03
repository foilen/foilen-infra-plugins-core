/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.machine;

import java.util.Arrays;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenMachinePluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinitionV1 = new IPPluginDefinitionV1("Foilen", "Machine", "To manage machines", version);

        pluginDefinitionV1.addCustomResource(Machine.class, "Machine", //
                Arrays.asList(Machine.PROPERTY_NAME), //
                Arrays.asList( //
                        Machine.PROPERTY_NAME, //
                        Machine.PROPERTY_PUBLIC_IP //
                ));

        pluginDefinitionV1.addTranslations("/com/foilen/infra/resource/machine/messages");
        pluginDefinitionV1.addResourceEditor(new MachineEditor(), MachineEditor.EDITOR_NAME);

        return pluginDefinitionV1;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext) {
    }

}
