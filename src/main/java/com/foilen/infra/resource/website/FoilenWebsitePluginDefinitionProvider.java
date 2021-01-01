/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.website;

import java.util.Arrays;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenWebsitePluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinitionV1 = new IPPluginDefinitionV1("Foilen", "Website", "To manage websites", version);

        pluginDefinitionV1.addCustomResource(Website.class, Website.RESOURCE_TYPE, //
                Arrays.asList( //
                        Website.PROPERTY_NAME //
                ), //
                Arrays.asList( //
                        Website.PROPERTY_NAME, //
                        Website.PROPERTY_DOMAIN_NAMES //
                ));

        pluginDefinitionV1.addTranslations("/com/foilen/infra/resource/website/messages");
        pluginDefinitionV1.addResourceEditor(new WebsiteEditor(), WebsiteEditor.EDITOR_NAME);

        pluginDefinitionV1.addChangesHandler(new WebsiteChangesEventHandler());

        return pluginDefinitionV1;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext, InternalServicesContext internalServicesContext) {
    }

}
