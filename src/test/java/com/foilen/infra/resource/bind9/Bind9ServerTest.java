/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.dns.DnsEntry;
import com.foilen.infra.resource.dns.ManualDnsEntryEditor;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tools.JsonTools;

public class Bind9ServerTest extends AbstractCorePluginTest {

    @Test
    public void test() {

        // Create resources
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser unixUser = new UnixUser(72000L, "infra-bind", "/home/infra-bind", null, null);

        Bind9Server bind9Server = new Bind9Server();
        bind9Server.setAdminEmail("admin@example.com");
        bind9Server.setName("myDns");
        bind9Server.setNsDomainNames(Arrays.asList("ns1.example.com", "ns2.example.com").stream().collect(Collectors.toCollection(() -> new TreeSet<>())));
        bind9Server.setResourceEditorName(Bind9ServerEditor.EDITOR_NAME);

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(machine);
        changes.resourceAdd(unixUser);
        changes.resourceAdd(bind9Server);

        // Create links
        changes.linkAdd(bind9Server, LinkTypeConstants.RUN_AS, unixUser);
        changes.linkAdd(bind9Server, LinkTypeConstants.INSTALLED_ON, machine);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Change serial
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
        String currentSerial = sdf.format(new Date()) + "00";
        String newSerial = "1111111100";
        Application application = resourceServicesInMemoryImpl.getResources().stream() //
                .filter(it -> it.getClass().equals(Application.class) && "myDns_bind9".equals(it.getResourceName())) //
                .map(it -> (Application) it) //
                .findAny().get();
        String json = JsonTools.compactPrint(application.getApplicationDefinition());
        application.setApplicationDefinition(JsonTools.readFromString(json.replaceAll(currentSerial, newSerial), IPApplicationDefinition.class));

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "Bind9ServerTest-state-1.json", getClass(), true);

        // Add DNS Entry using the editor
        Map<String, String> formValues = new HashMap<>();
        formValues.put(DnsEntry.PROPERTY_NAME, "editor.example.com");
        formValues.put(DnsEntry.PROPERTY_TYPE, "A");
        formValues.put(DnsEntry.PROPERTY_DETAILS, "127.0.0.1");
        assertEditorNoErrors(null, new ManualDnsEntryEditor(), formValues);

        // Change serial
        application = resourceServicesInMemoryImpl.getResources().stream() //
                .filter(it -> it.getClass().equals(Application.class) && "myDns_bind9".equals(it.getResourceName())) //
                .map(it -> (Application) it) //
                .findAny().get();
        json = JsonTools.compactPrint(application.getApplicationDefinition());
        application.setApplicationDefinition(JsonTools.readFromString(json.replaceAll(currentSerial, newSerial), IPApplicationDefinition.class));

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "Bind9ServerTest-state-2.json", getClass(), true);
    }

}
