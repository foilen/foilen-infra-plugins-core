/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.plugin;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.smalltools.test.asserts.AssertTools;

public class LetsEncryptRefreshOldCertsBeginTimerTest extends AbstractCorePluginTest {

    @Test
    public void test_nothing() {

        // Add a machine
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(new Machine("test1.node.example.com", "192.168.0.11"));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
        changes.clear();

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "LetsEncryptRefreshOldCertsBeginTimerTest-test-state.json", getClass(), true);

        // Execute the timer
        LetsEncryptRefreshOldCertsBeginTimer timer = new LetsEncryptRefreshOldCertsBeginTimer();
        timer.timerHandler(getCommonServicesContext(), changes, null);

        // Assert no changes
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "LetsEncryptRefreshOldCertsBeginTimerTest-test-state.json", getClass(), true);
        AssertTools.assertJsonComparison(new ChangesContext(getCommonServicesContext().getResourceService()), changes);

    }

}
