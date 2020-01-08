/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.plugin;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenLetsencryptPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinition = new IPPluginDefinitionV1("Foilen", "Lets Encrypt", "Automatically retrieve letsencrypt certificates", version);

        pluginDefinition.addCustomResource(LetsencryptConfig.class, LetsencryptConfig.RESOURCE_TYPE, //
                Arrays.asList(LetsencryptConfig.PROPERTY_NAME), //
                Collections.emptyList());
        pluginDefinition.addCustomResource(LetsEncryptWithFileAttachable.class, LetsEncryptWithFileAttachable.RESOURCE_TYPE, //
                Arrays.asList(LetsEncryptWithFileAttachable.PROPERTY_NAME), //
                Collections.emptyList());

        pluginDefinition.addTimer(new LetsEncryptRefreshOldCertsBeginTimer(), //
                LetsEncryptRefreshOldCertsBeginTimer.TIMER_NAME, //
                Calendar.DAY_OF_YEAR, //
                1, //
                false, //
                true);

        // Resource editors
        pluginDefinition.addTranslations("/com/foilen/infra/resource/letsencrypt/messages");
        pluginDefinition.addResourceEditor(new LetsencryptConfigEditor(), LetsencryptConfigEditor.EDITOR_NAME);
        pluginDefinition.addResourceEditor(new LetsEncryptWebsiteCertificateEditor(), LetsEncryptWebsiteCertificateEditor.EDITOR_NAME);
        pluginDefinition.addResourceEditor(new LetsEncryptWithFileAttachableEditor(), LetsEncryptWithFileAttachableEditor.EDITOR_NAME);

        // Updater
        pluginDefinition.addChangesHandler(new LetsencryptConfigChangesEventHandler());
        pluginDefinition.addChangesHandler(new LetsencryptWithFileAttachableChangesEventHandler());

        return pluginDefinition;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext, InternalServicesContext internalServicesContext) {
    }

}
