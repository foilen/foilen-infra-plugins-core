/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.core.system.junits.ResourcesDump;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.email.resources.EmailDomain;
import com.foilen.infra.resource.email.resources.JamesEmailServer;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.smalltools.tools.JsonTools;

public class JamesEmailServerTest extends AbstractCorePluginTest {

    private Map<String, String> getEmailServerMeta() {
        return getInternalServicesContext().getInternalIPResourceService().resourceFindAll().stream() //
                .filter(it -> it instanceof JamesEmailServer) //
                .map(it -> {
                    JamesEmailServer jamesEmailServer = (JamesEmailServer) it;
                    return jamesEmailServer.getMeta();
                }) //
                .findFirst().get();

    }

    private void removeBinaryFiles() {
        getInternalServicesContext().getInternalIPResourceService().resourceFindAll().stream() //
                .filter(it -> it instanceof Application) //
                .forEach(it -> {
                    Application application = (Application) it;
                    application.getApplicationDefinition().getAssetsBundles().forEach(bundle -> {
                        bundle.getAssetsRelativePathAndBinaryContent().forEach(file -> file.setB(new byte[] {}));
                    });
                });
    }

    @Test
    public void test_basic() {

        List<String> metaCertKeys = Arrays.asList( //
                "certKeystore-imaps", "certKeystore-pop3s", "certKeystore-smtp", //
                "certThumbprint-imaps", "certThumbprint-pop3s", "certThumbprint-smtp" //
        );

        IPResourceService resourceService = getCommonServicesContext().getResourceService();

        // Execute the dump
        ResourcesDump resourcesDump = JsonTools.readFromResource("JamesEmailServerTest_test_basic-import.json", ResourcesDump.class, getClass());
        JunitsHelper.dumpImport(getCommonServicesContext(), getInternalServicesContext(), resourcesDump);

        // Remove the binary files that are always changing
        removeBinaryFiles();

        // Remove and get meta
        Map<String, String> meta = getEmailServerMeta();
        Map<String, String> metaCloned = JsonTools.clone(meta);
        Assert.assertEquals(6, meta.size());
        metaCertKeys.forEach(key -> {
            Assert.assertNotNull(meta.remove(key));
        });
        Assert.assertEquals(0, meta.size());

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "JamesEmailServerTest_test_basic-state-allSameHost.json", getClass(), true);

        // Put back meta
        metaCertKeys.forEach(key -> {
            meta.put(key, metaCloned.get(key));
        });

        // Change for all different hosts
        Optional<EmailDomain> emailDomainOptional = resourceService.resourceFind(resourceService.createResourceQuery(EmailDomain.class));
        EmailDomain emailDomain = emailDomainOptional.get();
        emailDomain.setImapDomainName("imap.example.com");
        emailDomain.setPop3DomainName("pop3.example.com");
        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceUpdate(emailDomain);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Remove the binary files that are always changing
        removeBinaryFiles();

        // Remove and assert same meta
        Map<String, String> metaAfter = getEmailServerMeta();
        Assert.assertEquals(6, metaAfter.size());
        metaCertKeys.forEach(key -> {
            Assert.assertNotNull(metaCloned.get(key), metaAfter.remove(key));
        });
        Assert.assertEquals(0, metaAfter.size());

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "JamesEmailServerTest_test_basic-state-allDifferentHosts.json", getClass(), true);

    }

    @Test
    public void test_basic_debug_mode() {

        List<String> metaCertKeys = Arrays.asList( //
                "certKeystore-imaps", "certKeystore-pop3s", "certKeystore-smtp", //
                "certThumbprint-imaps", "certThumbprint-pop3s", "certThumbprint-smtp" //
        );

        IPResourceService resourceService = getCommonServicesContext().getResourceService();

        // Execute the dump
        ResourcesDump resourcesDump = JsonTools.readFromResource("JamesEmailServerTest_test_basic-import.json", ResourcesDump.class, getClass());
        JunitsHelper.dumpImport(getCommonServicesContext(), getInternalServicesContext(), resourcesDump);

        // Change the server to be in debug mode
        JamesEmailServer jamesEmailServer = resourceService.resourceFind(resourceService.createResourceQuery(JamesEmailServer.class)).get();
        jamesEmailServer.setEnableDebugDumpMessagesDetails(true);
        jamesEmailServer.setEnableDebuglogs(true);
        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceUpdate(jamesEmailServer);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Remove the binary files that are always changing
        removeBinaryFiles();

        // Remove and get meta
        Map<String, String> meta = getEmailServerMeta();
        Map<String, String> metaCloned = JsonTools.clone(meta);
        Assert.assertEquals(6, meta.size());
        metaCertKeys.forEach(key -> {
            Assert.assertNotNull(meta.remove(key));
        });
        Assert.assertEquals(0, meta.size());

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "JamesEmailServerTest_test_basic_debug_mode-state-allSameHost.json", getClass(), true);

        // Put back meta
        metaCertKeys.forEach(key -> {
            meta.put(key, metaCloned.get(key));
        });

        // Change for all different hosts
        Optional<EmailDomain> emailDomainOptional = resourceService.resourceFind(resourceService.createResourceQuery(EmailDomain.class));
        EmailDomain emailDomain = emailDomainOptional.get();
        emailDomain.setImapDomainName("imap.example.com");
        emailDomain.setPop3DomainName("pop3.example.com");
        changes = new ChangesContext(resourceService);
        changes.resourceUpdate(emailDomain);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Remove the binary files that are always changing
        removeBinaryFiles();

        // Remove and assert same meta
        Map<String, String> metaAfter = getEmailServerMeta();
        Assert.assertEquals(6, metaAfter.size());
        metaCertKeys.forEach(key -> {
            Assert.assertNotNull(metaCloned.get(key), metaAfter.remove(key));
        });
        Assert.assertEquals(0, metaAfter.size());

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "JamesEmailServerTest_test_basic_debug_mode-state-allDifferentHosts.json", getClass(), true);

    }

}
