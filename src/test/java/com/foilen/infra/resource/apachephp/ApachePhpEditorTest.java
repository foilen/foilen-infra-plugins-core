/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

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
        apachePhpEditorForm.put(ApachePhp.PROPERTY_LOG_MAX_SIZE_M, "10");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_MAX_UPLOAD_FILESIZE_M, "100");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_EMAIL_SENDER, EmailSender.MSMTP.name());
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
        apachePhpEditorForm.put(ApachePhp.PROPERTY_LOG_MAX_SIZE_M, "10");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_MAX_UPLOAD_FILESIZE_M, "100");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_EMAIL_SENDER, EmailSender.SENDMAIL.name());
        apachePhpEditorForm.put("unixUser", unixUserId);
        apachePhpEditorForm.put("machines", machineId);
        assertEditorNoErrors(null, new ApachePhpEditor(), apachePhpEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpEditorTest-test_sendmail-state.json", getClass(), true);

    }

    @Test
    public void test_sendmail_2() {

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
        apachePhpEditorForm.put(ApachePhp.PROPERTY_LOG_MAX_SIZE_M, "10");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_MAX_UPLOAD_FILESIZE_M, "100");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_EMAIL_SENDER, EmailSender.SENDMAIL.name());
        apachePhpEditorForm.put(ApachePhp.PROPERTY_DEFAULT_EMAIL_FROM, "");
        apachePhpEditorForm.put("unixUser", unixUserId);
        apachePhpEditorForm.put("machines", machineId);
        assertEditorNoErrors(null, new ApachePhpEditor(), apachePhpEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpEditorTest-test_sendmail-state.json", getClass(), true);

    }

    @Test
    public void test_sendmail_to_msmtp() {

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
        apachePhpEditorForm.put(ApachePhp.PROPERTY_LOG_MAX_SIZE_M, "10");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_MAX_UPLOAD_FILESIZE_M, "100");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_EMAIL_SENDER, EmailSender.SENDMAIL_TO_MSMTP.name());
        apachePhpEditorForm.put("unixUser", unixUserId);
        apachePhpEditorForm.put("machines", machineId);
        assertEditorNoErrors(null, new ApachePhpEditor(), apachePhpEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpEditorTest-test_sendmail_to_msmtp-state.json", getClass(), true);

    }

    @Test
    public void test_sendmail_to_msmtp_no_max_upload() {

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
        apachePhpEditorForm.put(ApachePhp.PROPERTY_LOG_MAX_SIZE_M, "10");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_MAX_UPLOAD_FILESIZE_M, "");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_EMAIL_SENDER, EmailSender.SENDMAIL_TO_MSMTP.name());
        apachePhpEditorForm.put("unixUser", unixUserId);
        apachePhpEditorForm.put("machines", machineId);
        assertEditorNoErrors(null, new ApachePhpEditor(), apachePhpEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpEditorTest-test_sendmail_to_msmtp_no_max_upload-state.json", getClass(), true);

    }

    @Test
    public void test_sendmail_to_msmtp_unlimited_max_upload() {

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
        apachePhpEditorForm.put(ApachePhp.PROPERTY_LOG_MAX_SIZE_M, "10");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_MAX_UPLOAD_FILESIZE_M, "0");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_EMAIL_SENDER, EmailSender.SENDMAIL_TO_MSMTP.name());
        apachePhpEditorForm.put("unixUser", unixUserId);
        apachePhpEditorForm.put("machines", machineId);
        assertEditorNoErrors(null, new ApachePhpEditor(), apachePhpEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpEditorTest-test_sendmail_to_msmtp_unlimited_max_upload-state.json", getClass(), true);

    }

    @Test
    public void test_sendmail_to_msmtp_big_max_upload() {

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
        apachePhpEditorForm.put(ApachePhp.PROPERTY_LOG_MAX_SIZE_M, "10");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_MAX_UPLOAD_FILESIZE_M, "5000");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_EMAIL_SENDER, EmailSender.SENDMAIL_TO_MSMTP.name());
        apachePhpEditorForm.put("unixUser", unixUserId);
        apachePhpEditorForm.put("machines", machineId);
        assertEditorNoErrors(null, new ApachePhpEditor(), apachePhpEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpEditorTest-test_sendmail_to_msmtp_big_max_upload-state.json", getClass(), true);

    }

    @Test
    public void test_sendmail_to_msmtp_with_default_email() {

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
        apachePhpEditorForm.put(ApachePhp.PROPERTY_LOG_MAX_SIZE_M, "10");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_MAX_UPLOAD_FILESIZE_M, "100");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_EMAIL_SENDER, EmailSender.SENDMAIL_TO_MSMTP.name());
        apachePhpEditorForm.put(ApachePhp.PROPERTY_DEFAULT_EMAIL_FROM, "admin@example.com");
        apachePhpEditorForm.put("unixUser", unixUserId);
        apachePhpEditorForm.put("machines", machineId);
        assertEditorNoErrors(null, new ApachePhpEditor(), apachePhpEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpEditorTest-test_sendmail_to_msmtp_with_default_email-state.json", getClass(), true);

    }

    @Test
    public void test_sendmail_with_default_email() {

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
        apachePhpEditorForm.put(ApachePhp.PROPERTY_LOG_MAX_SIZE_M, "10");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_MAX_UPLOAD_FILESIZE_M, "100");
        apachePhpEditorForm.put(ApachePhp.PROPERTY_EMAIL_SENDER, EmailSender.SENDMAIL.name());
        apachePhpEditorForm.put(ApachePhp.PROPERTY_DEFAULT_EMAIL_FROM, "admin@example.com");
        apachePhpEditorForm.put("unixUser", unixUserId);
        apachePhpEditorForm.put("machines", machineId);
        assertEditorNoErrors(null, new ApachePhpEditor(), apachePhpEditorForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpEditorTest-test_sendmail_with_default_email-state.json", getClass(), true);

    }

}
