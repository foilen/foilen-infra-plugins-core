/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.usagemetrics;

import java.util.Arrays;
import java.util.Collections;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.infra.resource.usagemetrics.editors.UsageMetricsConfigEditor;
import com.foilen.infra.resource.usagemetrics.handlers.UsageMetricsConfigChangesEventHandler;
import com.foilen.infra.resource.usagemetrics.resources.UsageMetricsConfig;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenUsageMetricsPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinition = new IPPluginDefinitionV1("Foilen", "Usage Metrics", "To manage the usage metrics", version);

        pluginDefinition.addCustomResource(UsageMetricsConfig.class, UsageMetricsConfig.RESOURCE_TYPE, //
                Arrays.asList(UsageMetricsConfig.PROPERTY_UID), //
                Collections.emptyList());

        // Resource editors
        pluginDefinition.addTranslations("/com/foilen/infra/resource/usagemetrics/messages");
        pluginDefinition.addResourceEditor(new UsageMetricsConfigEditor(), UsageMetricsConfigEditor.EDITOR_NAME);

        // Updater Handler
        pluginDefinition.addChangesHandler(new UsageMetricsConfigChangesEventHandler());

        return pluginDefinition;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext, InternalServicesContext internalServicesContext) {
    }

}
