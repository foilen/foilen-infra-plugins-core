/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.machine;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.resource.test.AbstractCorePluginTest;

public class MachineEditorTest extends AbstractCorePluginTest {

    private Machine findMachineByName(String name) {
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        return resourceService.resourceFind(resourceService.createResourceQuery(Machine.class) //
                .propertyEquals(Machine.PROPERTY_NAME, name)) //
                .get();
    }

    @Test
    public void test_cannot_update_ip() {

        // Create fake data
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        InternalChangeService internalChangeService = getInternalServicesContext().getInternalChangeService();

        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceAdd(new Machine("test1.node.example.com", "192.168.0.11"));
        internalChangeService.changesExecute(changes);
        String machineId = String.valueOf(findMachineByName("test1.node.example.com").getInternalId());

        // MachineEditor
        Map<String, String> machineEditorForm = new HashMap<>();
        machineEditorForm.put(Machine.PROPERTY_NAME, "test1.node.example.com");
        machineEditorForm.put(Machine.PROPERTY_PUBLIC_IP, "192.168.0.12");
        assertEditorWithErrors(machineId, new MachineEditor(), machineEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "MachineEditorTest-test_no_changes_update-state.json", getClass(), true);

    }

    @Test
    public void test_cannot_update_name() {

        // Create fake data
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        InternalChangeService internalChangeService = getInternalServicesContext().getInternalChangeService();

        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceAdd(new Machine("test1.node.example.com", "192.168.0.11"));
        internalChangeService.changesExecute(changes);
        String machineId = String.valueOf(findMachineByName("test1.node.example.com").getInternalId());

        // MachineEditor
        Map<String, String> machineEditorForm = new HashMap<>();
        machineEditorForm.put(Machine.PROPERTY_NAME, "test2.node.example.com");
        machineEditorForm.put(Machine.PROPERTY_PUBLIC_IP, "192.168.0.11");
        assertEditorWithErrors(machineId, new MachineEditor(), machineEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "MachineEditorTest-test_no_changes_update-state.json", getClass(), true);

    }

    @Test
    public void test_no_changes_update() {

        // Create fake data
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        InternalChangeService internalChangeService = getInternalServicesContext().getInternalChangeService();

        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceAdd(new Machine("test1.node.example.com", "192.168.0.11"));
        internalChangeService.changesExecute(changes);
        String machineId = String.valueOf(findMachineByName("test1.node.example.com").getInternalId());

        // MachineEditor
        Map<String, String> machineEditorForm = new HashMap<>();
        machineEditorForm.put(Machine.PROPERTY_NAME, "test1.node.example.com");
        machineEditorForm.put(Machine.PROPERTY_PUBLIC_IP, "192.168.0.11");
        assertEditorNoErrors(machineId, new MachineEditor(), machineEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "MachineEditorTest-test_no_changes_update-state.json", getClass(), true);

    }

}
