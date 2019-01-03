/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import java.util.Arrays;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenDnsPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinitionV1 = new IPPluginDefinitionV1("Foilen", "DNS", "To manage DNS entries", version);

        pluginDefinitionV1.addCustomResource(DnsEntry.class, DnsEntry.RESOURCE_TYPE, //
                Arrays.asList( //
                        DnsEntry.PROPERTY_NAME, //
                        DnsEntry.PROPERTY_TYPE, //
                        DnsEntry.PROPERTY_DETAILS //
                ), //
                Arrays.asList( //
                        DnsEntry.PROPERTY_NAME, //
                        DnsEntry.PROPERTY_TYPE, //
                        DnsEntry.PROPERTY_DETAILS //
                ));

        pluginDefinitionV1.addCustomResource(DnsPointer.class, DnsPointer.RESOURCE_TYPE, //
                Arrays.asList( //
                        DnsPointer.PROPERTY_NAME //
                ), //
                Arrays.asList( //
                        DnsPointer.PROPERTY_NAME //
                ));

        pluginDefinitionV1.addTranslations("/com/foilen/infra/resource/dns/messages");
        pluginDefinitionV1.addResourceEditor(new ManualDnsEntryEditor(), ManualDnsEntryEditor.EDITOR_NAME);

        pluginDefinitionV1.addUpdateHandler(new DnsEntryValidationUpdateHandler());
        pluginDefinitionV1.addUpdateHandler(new DnsPointerUpdateHandler());
        pluginDefinitionV1.addUpdateHandler(new MachineUpdateHandler());

        return pluginDefinitionV1;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext) {
    }

}
