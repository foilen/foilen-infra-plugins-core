/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email;

import java.util.Optional;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.core.system.junits.ResourcesDump;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.email.resources.EmailDomain;
import com.foilen.infra.resource.email.resources.JamesEmailServer;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.smalltools.tools.JsonTools;

public class JamesEmailServerTest extends AbstractCorePluginTest {

    @Test
    public void test_basic() {

        IPResourceService resourceService = getCommonServicesContext().getResourceService();

        // Execute the dump
        ResourcesDump resourcesDump = JsonTools.readFromResource("JamesEmailServerTest_test_basic-import.json", ResourcesDump.class, getClass());
        JunitsHelper.dumpImport(getCommonServicesContext(), getInternalServicesContext(), resourcesDump);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "JamesEmailServerTest_test_basic-state-allSameHost.json", getClass(), true);

        // Change for all different hosts
        Optional<EmailDomain> emailDomainOptional = resourceService.resourceFind(resourceService.createResourceQuery(EmailDomain.class));
        EmailDomain emailDomain = emailDomainOptional.get();
        emailDomain.setImapDomainName("imap.example.com");
        emailDomain.setPop3DomainName("pop3.example.com");
        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceUpdate(emailDomain);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "JamesEmailServerTest_test_basic-state-allDifferentHosts.json", getClass(), true);

    }

    @Test
    public void test_basic_debug_mode() {

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

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "JamesEmailServerTest_test_basic_debug_mode-state-allSameHost.json", getClass(), true);

        // Change for all different hosts
        Optional<EmailDomain> emailDomainOptional = resourceService.resourceFind(resourceService.createResourceQuery(EmailDomain.class));
        EmailDomain emailDomain = emailDomainOptional.get();
        emailDomain.setImapDomainName("imap.example.com");
        emailDomain.setPop3DomainName("pop3.example.com");
        changes = new ChangesContext(resourceService);
        changes.resourceUpdate(emailDomain);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "JamesEmailServerTest_test_basic_debug_mode-state-allDifferentHosts.json", getClass(), true);

    }

}
