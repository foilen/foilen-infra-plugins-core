/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import java.util.Arrays;
import java.util.Collections;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenApachePhpPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinition = new IPPluginDefinitionV1("Foilen", "Apache and PHP", "To create web sites", version);

        pluginDefinition.addCustomResource(ApachePhp.class, ApachePhp.RESOURCE_TYPE, //
                Arrays.asList(ApachePhp.PROPERTY_NAME), //
                Arrays.asList(ApachePhp.PROPERTY_VERSION));
        pluginDefinition.addCustomResource(ApachePhpFolder.class, ApachePhpFolder.RESOURCE_TYPE, true, //
                Arrays.asList(ApachePhpFolder.PROPERTY_UID), //
                Collections.emptyList());
        pluginDefinition.addCustomResource(ApachePhpHtPasswd.class, ApachePhpHtPasswd.RESOURCE_TYPE, true, //
                Arrays.asList(ApachePhpHtPasswd.PROPERTY_UID), //
                Collections.emptyList());

        // Resource editors
        pluginDefinition.addTranslations("/com/foilen/infra/resource/apachephp/messages");
        pluginDefinition.addResourceEditor(new ApachePhpEditor(), ApachePhpEditor.EDITOR_NAME);
        pluginDefinition.addResourceEditor(new ApachePhpFolderEditor(), ApachePhpFolderEditor.EDITOR_NAME);
        pluginDefinition.addResourceEditor(new ApachePhpHtPasswdEditor(), ApachePhpHtPasswdEditor.EDITOR_NAME);

        // Updater Handler
        pluginDefinition.addChangesHandler(new ApachePhpChangesEventHandler());

        return pluginDefinition;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext, InternalServicesContext internalServicesContext) {
    }

}
