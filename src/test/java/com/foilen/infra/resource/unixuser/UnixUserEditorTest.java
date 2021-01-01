/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.unixuser;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.test.AbstractCorePluginTest;

public class UnixUserEditorTest extends AbstractCorePluginTest {

    private UnixUserEditor unixUserEditor = new UnixUserEditor();

    private UnixUser findUnixUser(String name) {
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        return resourceService.resourceFind(resourceService.createResourceQuery(UnixUser.class).propertyEquals(UnixUser.PROPERTY_NAME, name)).orElse(null);
    }

    @Test
    public void test() {

        // ---------- Create without password ----------
        assertEditorPageDefinition(UnixUserEditor.EDITOR_NAME, null, "UnixUserEditorTest-01-pageDefinition.json", getClass());

        Map<String, String> formValues = new HashMap<>();
        formValues.put("name", "the_user");
        formValues.put("password", "");
        formValues.put("passwordConf", "");
        formValues.put("keepClearPassword", "false");
        formValues.put("machines", "");
        assertEditorNoErrors(null, unixUserEditor, formValues);

        // Assert
        UnixUser actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertNull(actual.getPassword());
        Assert.assertFalse(actual.isKeepClearPassword());
        Assert.assertNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());

        // ---------- Add a password ----------
        assertEditorPageDefinition(UnixUserEditor.EDITOR_NAME, actual, "UnixUserEditorTest-02-pageDefinition.json", getClass());

        formValues = new HashMap<>();
        formValues.put("name", "the_user");
        formValues.put("password", "qwerty");
        formValues.put("passwordConf", "qwerty");
        formValues.put("keepClearPassword", "false");
        formValues.put("machines", "");
        assertEditorNoErrors(actual.getInternalId(), unixUserEditor, formValues);

        // Assert
        actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertNull(actual.getPassword());
        Assert.assertFalse(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());

        // ---------- Don't clear the password ----------
        assertEditorPageDefinition(UnixUserEditor.EDITOR_NAME, actual, "UnixUserEditorTest-03-pageDefinition.json", getClass());

        formValues = new HashMap<>();
        formValues.put("name", "the_user");
        formValues.put("password", "qwerty");
        formValues.put("passwordConf", "qwerty");
        formValues.put("keepClearPassword", "true");
        formValues.put("machines", "");
        assertEditorNoErrors(actual.getInternalId(), unixUserEditor, formValues);

        // Assert
        actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertEquals("qwerty", actual.getPassword());
        Assert.assertTrue(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());

        // ---------- Change the password ----------
        assertEditorPageDefinition(UnixUserEditor.EDITOR_NAME, actual, "UnixUserEditorTest-04-pageDefinition.json", getClass());

        formValues = new HashMap<>();
        formValues.put("name", "the_user");
        formValues.put("password", "qwerty2");
        formValues.put("passwordConf", "qwerty2");
        formValues.put("keepClearPassword", "true");
        formValues.put("machines", "");
        assertEditorNoErrors(actual.getInternalId(), unixUserEditor, formValues);

        // Assert
        actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertEquals("qwerty2", actual.getPassword());
        Assert.assertTrue(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());

        assertEditorPageDefinition(UnixUserEditor.EDITOR_NAME, actual, "UnixUserEditorTest-05-pageDefinition.json", getClass());
    }

}
