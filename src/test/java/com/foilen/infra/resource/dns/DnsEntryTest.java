/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.bind9.Bind9Server;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.unixuser.UnixUser;

public class DnsEntryTest extends AbstractCorePluginTest {

    @Test
    public void test_basic() {

        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        InternalChangeService internalChangeService = getInternalServicesContext().getInternalChangeService();

        // Create some
        ChangesContext changes = new ChangesContext(resourceService);
        for (int i = 0; i < 5; ++i) {
            changes.resourceAdd(new DnsEntry("d" + i + ".example.com", DnsEntryType.TXT, "hello"));
        }
        internalChangeService.changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "DnsEntryTest_test_basic-state-1.json", getClass(), true);

        // Update some
        DnsEntry dnsEntry = resourceService.resourceFindByPk(new DnsEntry("d1.example.com", DnsEntryType.TXT, "hello")).get();
        changes.resourceUpdate(dnsEntry, new DnsEntry("d1.example.com", DnsEntryType.TXT, "hello 2"));
        dnsEntry = resourceService.resourceFindByPk(new DnsEntry("d2.example.com", DnsEntryType.TXT, "hello")).get();
        changes.resourceUpdate(dnsEntry, new DnsEntry("u2.example.com", DnsEntryType.TXT, "hello"));
        internalChangeService.changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "DnsEntryTest_test_basic-state-2.json", getClass(), true);

        // Remove some
        changes.resourceDelete(resourceService.resourceFindByPk(new DnsEntry("d4.example.com", DnsEntryType.TXT, "hello")).get());
        internalChangeService.changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "DnsEntryTest_test_basic-state-3.json", getClass(), true);

        // Add SRV
        changes = new ChangesContext(resourceService);
        changes.resourceAdd(new DnsEntry("_sip._tls.example.com", DnsEntryType.SRV, "sipdir.online.lync.com").setPriority(100).setWeight(1).setPort(443));
        changes.resourceAdd(new DnsEntry("_sipfederationtls._tcp.example.com", DnsEntryType.SRV, "sipfed.online.lync.com").setPriority(100).setWeight(1).setPort(5061));
        internalChangeService.changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "DnsEntryTest_test_basic-state-4.json", getClass(), true);

        // Check app
        changes = new ChangesContext(resourceService);
        Machine machine = new Machine("m1.example.com");
        Bind9Server bind9Server = new Bind9Server("infra_bind").setAdminEmail("admin@example.com");
        bind9Server.getNsDomainNames().add("ns1.example.com");
        UnixUser unixuser = new UnixUser(72000L, "infrabind", "/home/infrabind", "/bin/bash");
        changes.resourceAdd(machine);
        changes.resourceAdd(bind9Server);
        changes.resourceAdd(unixuser);
        changes.linkAdd(unixuser, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(bind9Server, LinkTypeConstants.RUN_AS, unixuser);
        changes.linkAdd(bind9Server, LinkTypeConstants.INSTALLED_ON, machine);
        internalChangeService.changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "DnsEntryTest_test_basic-state-5.json", getClass(), true);

    }

}
