/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import com.foilen.infra.plugin.core.system.fake.junits.FakeSystemServicesTests;
import com.foilen.infra.plugin.core.system.fake.service.FakeSystemServicesImpl;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.smalltools.tools.TimeExecutionTools;

public class DnsEntryPerfTestingApp {

    public static void main(String[] args) {

        FakeSystemServicesImpl fakeSystemServices = FakeSystemServicesTests.init();

        // Create some
        long timeInMs = TimeExecutionTools.measureInMs(() -> {
            ChangesContext changes = new ChangesContext(fakeSystemServices);
            for (int i = 0; i < 500; ++i) {
                changes.resourceAdd(new DnsEntry("d" + i + ".example.com", DnsEntryType.TXT, "hello"));
                if (i % 50 == 0) {
                    fakeSystemServices.changesExecute(changes);
                }
            }
            fakeSystemServices.changesExecute(changes);
        });

        System.out.println("Took " + timeInMs + " ms");

    }

}
