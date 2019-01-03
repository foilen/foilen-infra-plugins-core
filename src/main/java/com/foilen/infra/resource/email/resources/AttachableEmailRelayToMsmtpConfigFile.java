/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.resources;

import java.util.Optional;

import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.msmtp.MsmtpConfig;
import com.foilen.infra.plugin.v1.model.outputter.msmtp.MsmtpConfigOutput;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.AttachablePartContext;
import com.foilen.infra.resource.composableapplication.ComposableApplication;

/**
 * To save a config file for msmtp on a {@link ComposableApplication} that redirects all mails to the {@link EmailRelay}.
 *
 * Link to:
 * <ul>
 * <li>{@link EmailRelay}: (1) POINTS_TO - Where to send the emails</li>
 * </ul>
 */
public class AttachableEmailRelayToMsmtpConfigFile extends AttachablePart {

    public static final String RESOURCE_TYPE = "Attachable Email Relay Msmtp Config File";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_CONFIG_PATH = "configPath";
    public static final String PROPERTY_USE_TLS = "useTls";

    // Details
    private String name;
    private String configPath = "/etc/msmtprc";
    private boolean useTls;

    @Override
    public void attachTo(AttachablePartContext context) {

        Optional<EmailRelay> emailRelayOptional = context.getServices().getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(this, LinkTypeConstants.POINTS_TO, EmailRelay.class)
                .stream().findAny();
        if (!emailRelayOptional.isPresent()) {
            return;
        }

        // Add configuration
        EmailRelay emailRelay = emailRelayOptional.get();
        MsmtpConfig msmtpConfig = new MsmtpConfig(emailRelay.getHostname(), emailRelay.getPort());
        msmtpConfig.setUsername(emailRelay.getUsername()).setPassword(emailRelay.getPassword());
        msmtpConfig.setTls(useTls);
        String configContent = MsmtpConfigOutput.toConfig(msmtpConfig);

        IPApplicationDefinition applicationDefinition = context.getApplicationDefinition();
        applicationDefinition.addAssetContent(configPath, configContent);

    }

    public String getConfigPath() {
        return configPath;
    }

    public String getName() {
        return name;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.EMAIL;
    }

    @Override
    public String getResourceDescription() {
        return "Saving to " + configPath;
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public boolean isUseTls() {
        return useTls;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUseTls(boolean useTls) {
        this.useTls = useTls;
    }

}
