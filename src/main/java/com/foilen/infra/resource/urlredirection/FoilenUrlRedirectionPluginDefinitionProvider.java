/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.urlredirection;

import java.util.Arrays;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenUrlRedirectionPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinitionV1 = new IPPluginDefinitionV1("Foilen", "Url Redirection", "To manage url redirections", version);

        pluginDefinitionV1.addCustomResource(UrlRedirection.class, UrlRedirection.RESOURCE_TYPE, //
                Arrays.asList( //
                        UrlRedirection.PROPERTY_DOMAIN_NAME //
                ), //
                Arrays.asList( //
                        UrlRedirection.PROPERTY_DOMAIN_NAME, //
                        UrlRedirection.PROPERTY_HTTP_REDIRECT_TO_URL, //
                        UrlRedirection.PROPERTY_HTTPS_REDIRECT_TO_URL //
                ));

        pluginDefinitionV1.addTranslations("/com/foilen/infra/resource/urlredirection/messages");

        pluginDefinitionV1.addResourceEditor(new UrlRedirectionEditor(), UrlRedirectionEditor.EDITOR_NAME);

        pluginDefinitionV1.addUpdateHandler(new AddRedirectionUnixUserUpdateHandler());
        pluginDefinitionV1.addUpdateHandler(new RedirectionWithApacheUpdateHandler());
        pluginDefinitionV1.addUpdateHandler(new WebsitesForUrlRedirectionUpdateHandler());

        return pluginDefinitionV1;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext) {
    }

}
