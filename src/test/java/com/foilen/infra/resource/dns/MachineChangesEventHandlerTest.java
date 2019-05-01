/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.test.AbstractCorePluginTest;

public class MachineChangesEventHandlerTest extends AbstractCorePluginTest {

    @Test
    public void test() {

        String machineName1 = "m1.node.example.com";
        String machineName2 = "m2.node.example.com";

        String m1Ip2 = "199.141.1.102";
        String m2Ip1 = "199.141.1.201";
        String m2Ip2 = "199.141.1.202";

        assertResourceCount(0, Machine.class);
        assertResourceExists(false, new Machine(machineName1), Machine.class);
        assertResourceExists(false, new Machine(machineName2), Machine.class);
        assertResourceCount(0, DnsEntry.class);

        // Create both machines: m1 without ip and m2 with ip
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(new Machine(machineName1));
        changes.resourceAdd(new Machine(machineName2, m2Ip1));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        assertResourceCount(2, Machine.class);
        Machine m1 = assertResourceExists(true, new Machine(machineName1), Machine.class);
        Machine m2 = assertResourceExists(true, new Machine(machineName2), Machine.class);
        Assert.assertEquals(null, m1.getPublicIp());
        Assert.assertEquals(m2Ip1, m2.getPublicIp());
        assertResourceCount(1, DnsEntry.class);
        assertResourceExists(true, new DnsEntry(machineName2, DnsEntryType.A, m2Ip1), DnsEntry.class);

        // Update both machines: m1 with ip and m2 with a different ip
        changes.resourceUpdate(m1.getInternalId(), new Machine(machineName1, m1Ip2));
        changes.resourceUpdate(m2.getInternalId(), new Machine(machineName2, m2Ip2));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        assertResourceCount(2, Machine.class);
        m1 = assertResourceExists(true, new Machine(machineName1), Machine.class);
        m2 = assertResourceExists(true, new Machine(machineName2), Machine.class);
        Assert.assertEquals(m1Ip2, m1.getPublicIp());
        Assert.assertEquals(m2Ip2, m2.getPublicIp());
        assertResourceCount(2, DnsEntry.class);
        assertResourceExists(true, new DnsEntry(machineName1, DnsEntryType.A, m1Ip2), DnsEntry.class);
        assertResourceExists(true, new DnsEntry(machineName2, DnsEntryType.A, m2Ip2), DnsEntry.class);

        // Remove ip of m2
        changes.resourceUpdate(m2.getInternalId(), new Machine(machineName2, null));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        assertResourceCount(2, Machine.class);
        m1 = assertResourceExists(true, new Machine(machineName1), Machine.class);
        m2 = assertResourceExists(true, new Machine(machineName2), Machine.class);
        Assert.assertEquals(m1Ip2, m1.getPublicIp());
        Assert.assertEquals(null, m2.getPublicIp());
        assertResourceCount(1, DnsEntry.class);
        assertResourceExists(true, new DnsEntry(machineName1, DnsEntryType.A, m1Ip2), DnsEntry.class);

        // Delete m1
        changes.resourceDelete(m1.getInternalId());
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        assertResourceCount(1, Machine.class);
        m2 = assertResourceExists(true, new Machine(machineName2), Machine.class);
        Assert.assertEquals(null, m2.getPublicIp());
        assertResourceCount(0, DnsEntry.class);

        // Update name (fails)
        try {
            changes.resourceUpdate(m2.getInternalId(), new Machine("anotherName.node.example.com"));
            getInternalServicesContext().getInternalChangeService().changesExecute(changes);
            Assert.fail("Expecting an exception");
        } catch (IllegalUpdateException e) {
        }
        changes.clear();

        // Rolled back
        assertResourceCount(1, Machine.class);
        m2 = assertResourceExists(true, new Machine(machineName2), Machine.class);
        Assert.assertEquals(null, m2.getPublicIp());
        assertResourceCount(0, DnsEntry.class);

    }

}
