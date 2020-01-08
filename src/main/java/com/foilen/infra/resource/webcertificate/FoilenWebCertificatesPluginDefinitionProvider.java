/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.webcertificate;

import java.util.Arrays;
import java.util.Calendar;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionProvider;
import com.foilen.infra.plugin.v1.core.plugin.IPPluginDefinitionV1;
import com.foilen.smalltools.tools.ResourceTools;

public class FoilenWebCertificatesPluginDefinitionProvider implements IPPluginDefinitionProvider {

    @Override
    public IPPluginDefinitionV1 getIPPluginDefinition() {

        String version = "N/A";
        try {
            version = ResourceTools.getResourceAsString("/foilen-infra-plugins-core.txt");
        } catch (Exception e) {
        }

        IPPluginDefinitionV1 pluginDefinitionV1 = new IPPluginDefinitionV1("Foilen", "Web Certificate", "To manage web certificates", version);

        pluginDefinitionV1.addCustomResource(WebsiteCertificate.class, WebsiteCertificate.RESOURCE_TYPE, //
                Arrays.asList(WebsiteCertificate.PROPERTY_THUMBPRINT), //
                Arrays.asList( //
                        WebsiteCertificate.PROPERTY_THUMBPRINT, //
                        WebsiteCertificate.PROPERTY_DOMAIN_NAMES, //
                        WebsiteCertificate.PROPERTY_CA_CERTIFICATE, //
                        WebsiteCertificate.PROPERTY_START, //
                        WebsiteCertificate.PROPERTY_END //
                ));

        pluginDefinitionV1.addTranslations("/com/foilen/infra/resource/webcertificate/messages");
        pluginDefinitionV1.addResourceEditor(new ManualWebsiteCertificateEditor(), ManualWebsiteCertificateEditor.EDITOR_NAME);
        pluginDefinitionV1.addResourceEditor(new SelfSignedWebsiteCertificateEditor(), SelfSignedWebsiteCertificateEditor.EDITOR_NAME);

        pluginDefinitionV1.addTimer(new SelfSignedWebsiteCertificateRefreshTimer(), //
                SelfSignedWebsiteCertificateRefreshTimer.TIMER_NAME, //
                Calendar.DAY_OF_YEAR, 1, //
                false, true);

        return pluginDefinitionV1;
    }

    @Override
    public void initialize(CommonServicesContext commonServicesContext, InternalServicesContext internalServicesContext) {
    }

}
