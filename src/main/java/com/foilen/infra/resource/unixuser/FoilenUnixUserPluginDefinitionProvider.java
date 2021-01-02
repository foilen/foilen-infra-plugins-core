/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.unixuser;

import java.util.Arrays;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.infra.resource.unixuser.helper.UnixUserAvailableIdHelper;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenUnixUserPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinitionV1 = new IPPluginDefinitionV1("Foilen", "Unix User", "To manage unix users", version);

        pluginDefinitionV1.addCustomResource(UnixUser.class, UnixUser.RESOURCE_TYPE, //
                Arrays.asList(UnixUser.PROPERTY_ID), //
                Arrays.asList( //
                        UnixUser.PROPERTY_NAME, //
                        UnixUser.PROPERTY_HOME_FOLDER, //
                        UnixUser.PROPERTY_SHELL //
                ));
        pluginDefinitionV1.addCustomResource(SystemUnixUser.class, SystemUnixUser.RESOURCE_TYPE, //
                Arrays.asList(UnixUser.PROPERTY_ID), //
                Arrays.asList( //
                        UnixUser.PROPERTY_NAME, //
                        UnixUser.PROPERTY_HOME_FOLDER, //
                        UnixUser.PROPERTY_SHELL //
                ));

        pluginDefinitionV1.addTranslations("/com/foilen/infra/resource/unixuser/messages");
        pluginDefinitionV1.addResourceEditor(new UnixUserEditor(), UnixUserEditor.EDITOR_NAME);
        pluginDefinitionV1.addResourceEditor(new SystemUnixUserEditor(), SystemUnixUserEditor.EDITOR_NAME);

        pluginDefinitionV1.addChangesHandler(new UnixUserChangesEventHandler());

        return pluginDefinitionV1;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext, InternalServicesContext internalServicesContext) {
        UnixUserAvailableIdHelper.init(commonServicesContext.getResourceService());
    }

}
