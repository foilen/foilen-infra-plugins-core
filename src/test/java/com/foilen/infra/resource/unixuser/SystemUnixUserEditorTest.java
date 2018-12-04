/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.unixuser;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.smalltools.tuple.Tuple2;

public class SystemUnixUserEditorTest extends AbstractCorePluginTest {

    private SystemUnixUserEditor systemUnixUserEditor = new SystemUnixUserEditor();

    private SystemUnixUser findSystemUnixUser(String name) {
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        return resourceService.resourceFind(resourceService.createResourceQuery(SystemUnixUser.class).propertyEquals(UnixUser.PROPERTY_NAME, name)).orElse(null);
    }

    @Test
    public void testIdBiggerThanMax_FAIL() {
        Map<String, String> formValues = new HashMap<>();
        formValues.put("id", "70000");
        formValues.put("name", "root");
        assertEditorWithErrors(null, systemUnixUserEditor, formValues, new Tuple2<>(UnixUser.PROPERTY_ID, "SystemUnixUserEditor.error.idIsTooHigh"));
    }

    @Test
    public void testNoId_FAIL() {
        Map<String, String> formValues = new HashMap<>();
        formValues.put("name", "root");
        assertEditorWithErrors(null, systemUnixUserEditor, formValues, new Tuple2<>(UnixUser.PROPERTY_ID, "error.required"));
    }

    @Test
    public void testRoot_OK() {
        assertEditorPageDefinition(SystemUnixUserEditor.EDITOR_NAME, null, "SystemUnixUserEditorTest-01-testRoot_OK.json", getClass());

        Map<String, String> formValues = new HashMap<>();
        formValues.put("id", "0");
        formValues.put("name", "root");
        assertEditorNoErrors(null, systemUnixUserEditor, formValues);

        assertEditorPageDefinition(SystemUnixUserEditor.EDITOR_NAME, findSystemUnixUser("root"), "SystemUnixUserEditorTest-02-testRoot_OK.json", getClass());
    }

}
