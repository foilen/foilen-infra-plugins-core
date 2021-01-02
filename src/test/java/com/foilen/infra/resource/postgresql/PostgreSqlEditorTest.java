/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.postgresql;

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

public class PostgreSqlEditorTest extends AbstractCorePluginTest {

    private Machine findMachineByName(String name) {
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        return resourceService.resourceFind(resourceService.createResourceQuery(Machine.class) //
                .propertyEquals(Machine.PROPERTY_NAME, name)) //
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

        // PostgreSqlServerEditor
        Map<String, String> postgreSqlServerEditorForm = new HashMap<>();
        postgreSqlServerEditorForm.put(PostgreSqlServer.PROPERTY_NAME, "user_db");
        postgreSqlServerEditorForm.put(PostgreSqlServer.PROPERTY_VERSION, "11.1-2");
        postgreSqlServerEditorForm.put(PostgreSqlServer.PROPERTY_AUTH_METHOD, "md5");
        postgreSqlServerEditorForm.put(PostgreSqlServer.PROPERTY_ROOT_PASSWORD, "abc");
        postgreSqlServerEditorForm.put("unixUser", unixUserId);
        postgreSqlServerEditorForm.put("machine", machineId);
        assertEditorNoErrors(null, new PostgreSqlServerEditor(), postgreSqlServerEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "PostgreSqlEditorTest-test-state-1.json", getClass(), true);

    }

}
