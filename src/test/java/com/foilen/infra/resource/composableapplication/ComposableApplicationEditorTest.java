/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.test.util.ReflectionTestUtils;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.resource.composableapplication.parts.AttachableAptInstall;
import com.foilen.infra.resource.composableapplication.parts.AttachableAptInstallEditor;
import com.foilen.infra.resource.composableapplication.parts.AttachableContainerUserToChangeId;
import com.foilen.infra.resource.composableapplication.parts.AttachableContainerUserToChangeIdEditor;
import com.foilen.infra.resource.composableapplication.parts.AttachableService;
import com.foilen.infra.resource.composableapplication.parts.AttachableServiceEditor;
import com.foilen.infra.resource.composableapplication.parts.AttachableVolume;
import com.foilen.infra.resource.composableapplication.parts.AttachableVolumeEditor;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.google.common.base.Joiner;

public class ComposableApplicationEditorTest extends AbstractCorePluginTest {

    private <T extends AttachablePart> T findAttachablePart(String propertyName, String propertyValue, Class<T> type) {
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        return resourceService.resourceFind(resourceService.createResourceQuery(type) //
                .propertyEquals(propertyName, propertyValue)) //
                .get();
    }

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
        AttachableService attachableService = new AttachableService("java-app", "java -jar myapp.jar", null);
        attachableService.setResourceEditorName(AttachableServiceEditor.EDITOR_NAME);
        changes.resourceAdd(attachableService);
        internalChangeService.changesExecute(changes);

        // Fill the form
        ComposableApplicationEditor resourceEditor = new ComposableApplicationEditor();

        Map<String, String> formValues = new HashMap<>();
        formValues.put(ComposableApplication.PROPERTY_NAME, "my_app");
        formValues.put(ComposableApplication.PROPERTY_FROM, "ubuntu:16.04");
        formValues.put("unixUser", String.valueOf(findUnixUserByName("user1").getInternalId()));
        formValues.put("machines", String.valueOf(findMachineByName("test1.node.example.com").getInternalId()));
        formValues.put("attachableParts", String.valueOf(findAttachablePart(AttachableService.PROPERTY_NAME, "java-app", AttachableService.class).getInternalId()));

        assertEditorNoErrors(null, resourceEditor, formValues);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ComposableApplicationEditorTest-test-state.json", getClass(), true);
    }

    @Test
    public void test_java() {

        // Create fake data
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        InternalChangeService internalChangeService = getInternalServicesContext().getInternalChangeService();

        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceAdd(new Machine("test1.node.example.com", "192.168.0.11"));
        changes.resourceAdd(new UnixUser(null, "user1", "/home/user1", null, null));
        internalChangeService.changesExecute(changes);
        String unixUserId = String.valueOf(findUnixUserByName("user1").getInternalId());

        List<String> attachableParts = new ArrayList<>();

        // Attachable Container User To Change Id Form
        Map<String, String> attachableContainerUserToChangeIdForm = new HashMap<>();
        attachableContainerUserToChangeIdForm.put(AttachableContainerUserToChangeId.PROPERTY_USERNAME_IN_CONTAINER, "user1");
        attachableContainerUserToChangeIdForm.put("userToChangeIdTo", unixUserId);
        assertEditorNoErrors(null, new AttachableContainerUserToChangeIdEditor(), attachableContainerUserToChangeIdForm);
        attachableParts.add(String.valueOf(findAttachablePart(AttachableContainerUserToChangeId.PROPERTY_USERNAME_IN_CONTAINER, "user1", AttachableContainerUserToChangeId.class).getInternalId()));

        // Attachable Service Form
        Map<String, String> attachableServiceEditorForm = new HashMap<>();
        attachableServiceEditorForm.put(AttachableService.PROPERTY_NAME, "metrics_java_app");
        attachableServiceEditorForm.put(AttachableService.PROPERTY_COMMAND, "/usr/bin/java -jar metrics.jar prod");
        attachableServiceEditorForm.put(AttachableService.PROPERTY_WORKING_DIRECTORY, "/home/user1/metrics");
        assertEditorNoErrors(null, new AttachableServiceEditor(), attachableServiceEditorForm);
        attachableParts.add(String.valueOf(findAttachablePart(AttachableService.PROPERTY_NAME, "metrics_java_app", AttachableService.class).getInternalId()));

        // Attachable Apt Install Form
        Map<String, String> attachableAptInstallEditorForm = new HashMap<>();
        attachableAptInstallEditorForm.put(AttachableAptInstall.PROPERTY_NAME, "metrics_extra_packages");
        attachableAptInstallEditorForm.put(AttachableAptInstall.PROPERTY_PACKAGES + "[0]", "cron");
        attachableAptInstallEditorForm.put(AttachableAptInstall.PROPERTY_PACKAGES + "[1]", "vim");
        attachableAptInstallEditorForm.put(AttachableAptInstall.PROPERTY_PACKAGES + "[2]", "less");
        assertEditorNoErrors(null, new AttachableAptInstallEditor(), attachableAptInstallEditorForm);
        attachableParts.add(String.valueOf(findAttachablePart(AttachableAptInstall.PROPERTY_NAME, "metrics_extra_packages", AttachableAptInstall.class).getInternalId()));

        // Add Volumes
        Map<String, String> attachableVolumeEditorForm = new HashMap<>();
        attachableVolumeEditorForm.put(AttachableVolume.PROPERTY_HOST_FOLDER, "/home/user1/");
        attachableVolumeEditorForm.put(AttachableVolume.PROPERTY_CONTAINER_FOLDER, "/home/user1/");
        assertEditorNoErrors(null, new AttachableVolumeEditor(), attachableVolumeEditorForm);
        attachableParts.add(String.valueOf(findAttachablePart(AttachableVolume.PROPERTY_CONTAINER_FOLDER, "/home/user1", AttachableVolume.class).getInternalId()));

        attachableVolumeEditorForm = new HashMap<>();
        attachableVolumeEditorForm.put(AttachableVolume.PROPERTY_HOST_FOLDER, "");
        attachableVolumeEditorForm.put(AttachableVolume.PROPERTY_CONTAINER_FOLDER, "/var/log/");
        assertEditorNoErrors(null, new AttachableVolumeEditor(), attachableVolumeEditorForm);
        attachableParts.add(String.valueOf(findAttachablePart(AttachableVolume.PROPERTY_CONTAINER_FOLDER, "/var/log", AttachableVolume.class).getInternalId()));

        attachableVolumeEditorForm = new HashMap<>();
        attachableVolumeEditorForm.put(AttachableVolume.PROPERTY_HOST_FOLDER, null);
        attachableVolumeEditorForm.put(AttachableVolume.PROPERTY_CONTAINER_FOLDER, "/var/lock");
        assertEditorNoErrors(null, new AttachableVolumeEditor(), attachableVolumeEditorForm);
        attachableParts.add(String.valueOf(findAttachablePart(AttachableVolume.PROPERTY_CONTAINER_FOLDER, "/var/lock", AttachableVolume.class).getInternalId()));

        // Fix uid
        AtomicInteger nextUid = new AtomicInteger();
        getInternalServicesContext().getInternalIPResourceService().resourceFindAll().stream() //
                .filter(r -> {
                    try {
                        new BeanWrapperImpl(r).getPropertyDescriptor("uid");
                        return true;
                    } catch (InvalidPropertyException e) {
                        return false;
                    }
                }) //
                .sorted((a, b) -> a.getInternalId().compareTo(b.getInternalId())) //
                .forEach(it -> {
                    ReflectionTestUtils.setField(it, "uid", String.valueOf(nextUid.incrementAndGet()));
                    changes.resourceUpdate(it);
                });
        internalChangeService.changesExecute(changes);

        // Composable Application Form
        Map<String, String> composableApplicationForm = new HashMap<>();
        composableApplicationForm.put(ComposableApplication.PROPERTY_NAME, "my_java_app");
        composableApplicationForm.put(ComposableApplication.PROPERTY_FROM, "openjdk:8-jre-slim");
        composableApplicationForm.put(ComposableApplication.PROPERTY_MAIN_COMMAND, "/usr/bin/java -jar main.jar");
        composableApplicationForm.put(ComposableApplication.PROPERTY_MAIN_WORKING_DIRECTORY, "/home/user1/main");
        composableApplicationForm.put(ComposableApplication.PROPERTY_ENVIRONMENTS + "[0]", "MARIA_PORT=3306");
        composableApplicationForm.put(ComposableApplication.PROPERTY_ENVIRONMENTS + "[1]", "MARIA_HOST=127.0.0.1");
        composableApplicationForm.put(ComposableApplication.PROPERTY_PORTS_EXPOSED_TCP + "[0]", "3306:33060");
        composableApplicationForm.put(ComposableApplication.PROPERTY_PORTS_EXPOSED_UDP + "[0]", "53:53000");
        composableApplicationForm.put(ComposableApplication.PROPERTY_PORTS_ENDPOINT + "[0]", "53:DNS_UDP");
        composableApplicationForm.put("unixUser", unixUserId);
        composableApplicationForm.put("machines", String.valueOf(findMachineByName("test1.node.example.com").getInternalId()));

        composableApplicationForm.put("attachableParts", Joiner.on(',').join(attachableParts));

        assertEditorNoErrors(null, new ComposableApplicationEditor(), composableApplicationForm);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ComposableApplicationEditorTest-test_java-state.json", getClass(), true);
    }

}
