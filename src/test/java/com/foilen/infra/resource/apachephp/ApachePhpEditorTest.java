/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

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

public class ApachePhpEditorTest extends AbstractCorePluginTest {

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
    public void test_msmtp() {

        // Create fake data
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        InternalChangeService internalChangeService = getInternalServicesContext().getInternalChangeService();

        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceAdd(new Machine("test1.node.example.com", "192.168.0.11"));
        changes.resourceAdd(new UnixUser(null, "user1", "/home/user1", null, null));
        internalChangeService.changesExecute(changes);
        String machineId = String.valueOf(findMachineByName("test1.node.example.com").getInternalId());
        String unixUserId = String.valueOf(findUnixUserByName("user1").getInternalId());

        // ApachePhpEditor
        Map<String, String> apachePhpEditorForm = new HashMap<>();
        apachePhpEditorForm.put(ApachePhp.PROPERTY_NAME, "my_php");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_BASE_PATH, "/home/user1/php");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_MAIN_SITE_RELATIVE_PATH, "/");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_VERSION, "7.2.10-3");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_EMAIL_SENDER_MSMTP, "TRUE");
        apachePhpEditorForm.put("unixUser", unixUserId);
        apachePhpEditorForm.put("machines", machineId);
        assertEditorNoErrors(null, new ApachePhpEditor(), apachePhpEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpEditorTest-test_msmtp-state.json", getClass(), true);

    }

    @Test
    public void test_sendmail() {

        // Create fake data
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        InternalChangeService internalChangeService = getInternalServicesContext().getInternalChangeService();

        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceAdd(new Machine("test1.node.example.com", "192.168.0.11"));
        changes.resourceAdd(new UnixUser(null, "user1", "/home/user1", null, null));
        internalChangeService.changesExecute(changes);
        String machineId = String.valueOf(findMachineByName("test1.node.example.com").getInternalId());
        String unixUserId = String.valueOf(findUnixUserByName("user1").getInternalId());

        // ApachePhpEditor
        Map<String, String> apachePhpEditorForm = new HashMap<>();
        apachePhpEditorForm.put(ApachePhp.PROPERTY_NAME, "my_php");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_BASE_PATH, "/home/user1/php");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_MAIN_SITE_RELATIVE_PATH, "/");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_VERSION, "7.2.10-3");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_EMAIL_SENDER_MSMTP, "FALSE");
        apachePhpEditorForm.put("unixUser", unixUserId);
        apachePhpEditorForm.put("machines", machineId);
        assertEditorNoErrors(null, new ApachePhpEditor(), apachePhpEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpEditorTest-test_sendmail-state.json", getClass(), true);

    }

}
