/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionService;
import com.foilen.infra.resource.email.resources.EmailRelay;
import com.foilen.smalltools.tools.FreemarkerTools;

public class PostfixUtils {

    public static void addConfigAndServiceForRelay(String hostname, EmailRelay emailRelay, IPApplicationDefinition applicationDefinition) {

        IPApplicationDefinitionAssetsBundle assetsBundle = applicationDefinition.addAssetsBundle();

        // Add the config files
        Map<String, String> configFiles = getRelayConfig(hostname, emailRelay);
        configFiles.keySet().stream().sorted().forEach(fileName -> {
            assetsBundle.addAssetContent(fileName, configFiles.get(fileName));
        });
        assetsBundle.addAssetResource("/postfix-start.sh", "/com/foilen/infra/utils/postfix/postfix-start.sh");

        // Add the build step
        applicationDefinition.addBuildStepCommand("cd /etc/postfix/ && " //
                + "/usr/sbin/postmap sasl_passwd && " //
                + "chmod 0600 sasl_passwd sasl_passwd.db && " //
                + "chmod +x /postfix-start.sh "); //

        // Add service
        applicationDefinition.getServices().add(new IPApplicationDefinitionService("_postfix", "/postfix-start.sh", 0L));

    }

    /**
     * Get the configuration files for relaying all emails to a specific email server.
     *
     * @param hostname
     *            the local hostname
     * @param emailRelay
     *            the email relay
     * @return the filenames with their content
     */
    public static Map<String, String> getRelayConfig(String hostname, EmailRelay emailRelay) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("hostname", hostname);
        model.put("emailRelay", emailRelay);

        Map<String, String> configFiles = new TreeMap<>();
        configFiles.put("/etc/postfix/main.cf", FreemarkerTools.processTemplate("/com/foilen/infra/utils/postfix/main.cf.ftl", model));
        configFiles.put("/etc/postfix/sasl_passwd", "[" + emailRelay.getHostname() + "]:" + emailRelay.getPort() + " " + emailRelay.getUsername() + ":" + emailRelay.getPassword());

        return configFiles;
    }

}
