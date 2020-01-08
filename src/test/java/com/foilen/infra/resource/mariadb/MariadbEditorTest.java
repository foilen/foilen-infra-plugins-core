/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mariadb;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.unixuser.UnixUser;

public class MariadbEditorTest extends AbstractCorePluginTest {

    private Machine findMachineByName(String name) {
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        return resourceService.resourceFind(resourceService.createResourceQuery(Machine.class) //
                .propertyEquals(Machine.PROPERTY_NAME, name)) //
                .get();
    }

    private MariaDBDatabase findMariaDBDatabase(String name) {
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        return resourceService.resourceFind(resourceService.createResourceQuery(MariaDBDatabase.class) //
                .propertyEquals(MariaDBDatabase.PROPERTY_NAME, name)) //
                .get();
    }

    private MariaDBServer findMariaDBServer(String name) {
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        return resourceService.resourceFind(resourceService.createResourceQuery(MariaDBServer.class) //
                .propertyEquals(MariaDBServer.PROPERTY_NAME, name)) //
                .get();
    }

    private UnixUser findUnixUserByName(String name) {
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        return resourceService.resourceFind(resourceService.createResourceQuery(UnixUser.class) //
                .propertyEquals(UnixUser.PROPERTY_NAME, name)) //
                .get();
    }

    @Test
    public void test() {

        // Create fake data
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        InternalChangeService internalChangeService = getInternalServicesContext().getInternalChangeService();

        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceAdd(new Machine("test1.node.example.com", "192.168.0.11"));
        changes.resourceAdd(new UnixUser(null, "user1", "/home/user1", null, null));
        internalChangeService.changesExecute(changes);
        String machineId = String.valueOf(findMachineByName("test1.node.example.com").getInternalId());
        String unixUserId = String.valueOf(findUnixUserByName("user1").getInternalId());

        // MariaDBServerEditor
        Map<String, String> mariaDBServerEditorForm = new HashMap<>();
        mariaDBServerEditorForm.put(MariaDBServer.PROPERTY_NAME, "user_db");
        mariaDBServerEditorForm.put(MariaDBServer.PROPERTY_VERSION, "10.3.5-1.1.1-001");
        mariaDBServerEditorForm.put(MariaDBServer.PROPERTY_ROOT_PASSWORD, "abc");
        mariaDBServerEditorForm.put("unixUser", unixUserId);
        mariaDBServerEditorForm.put("machine", machineId);
        assertEditorNoErrors(null, new MariaDBServerEditor(), mariaDBServerEditorForm);
        String mariaDbServerId = String.valueOf(findMariaDBServer("user_db").getInternalId());

        // MariaDBDatabaseEditor
        Map<String, String> mariaDBDatabaseEditorForm = new HashMap<>();
        mariaDBDatabaseEditorForm.put(MariaDBDatabase.PROPERTY_NAME, "wordpress");
        mariaDBDatabaseEditorForm.put("mariadbServers", mariaDbServerId);
        assertEditorNoErrors(null, new MariaDBDatabaseEditor(), mariaDBDatabaseEditorForm);
        String mariaDbDatabaseId = String.valueOf(findMariaDBDatabase("wordpress").getInternalId());

        // MariaDBUserEditor
        Map<String, String> mariaDBUserEditorForm = new HashMap<>();
        mariaDBUserEditorForm.put(MariaDBUser.PROPERTY_NAME, "wp_user");
        mariaDBUserEditorForm.put(MariaDBUser.PROPERTY_PASSWORD, "123");
        mariaDBUserEditorForm.put("admin", mariaDbDatabaseId);
        mariaDBUserEditorForm.put("read", mariaDbDatabaseId);
        mariaDBUserEditorForm.put("write", mariaDbDatabaseId);
        assertEditorNoErrors(null, new MariaDBUserEditor(), mariaDBUserEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "MariadbEditorTest-test-state-1.json", getClass(), true);

        // MariaDBDatabaseEditor
        mariaDBDatabaseEditorForm = new HashMap<>();
        mariaDBDatabaseEditorForm.put(MariaDBDatabase.PROPERTY_NAME, "joomla");
        mariaDBDatabaseEditorForm.put("mariadbServers", mariaDbServerId);
        assertEditorNoErrors(null, new MariaDBDatabaseEditor(), mariaDBDatabaseEditorForm);
        mariaDbDatabaseId = String.valueOf(findMariaDBDatabase("joomla").getInternalId());

        // MariaDBUserEditor
        mariaDBUserEditorForm = new HashMap<>();
        mariaDBUserEditorForm.put(MariaDBUser.PROPERTY_NAME, "joomla_user");
        mariaDBUserEditorForm.put(MariaDBUser.PROPERTY_PASSWORD, "123");
        mariaDBUserEditorForm.put("admin", mariaDbDatabaseId);
        mariaDBUserEditorForm.put("read", mariaDbDatabaseId);
        mariaDBUserEditorForm.put("write", mariaDbDatabaseId);
        assertEditorNoErrors(null, new MariaDBUserEditor(), mariaDBUserEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "MariadbEditorTest-test-state-2.json", getClass(), true);
    }

}
