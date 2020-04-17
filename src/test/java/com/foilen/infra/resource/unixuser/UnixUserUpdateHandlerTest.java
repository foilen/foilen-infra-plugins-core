/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.unixuser;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.exception.ResourcePrimaryKeyCollisionException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.test.AbstractCorePluginTest;

public class UnixUserUpdateHandlerTest extends AbstractCorePluginTest {

    private UnixUser findUnixUser(String name) {
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        return resourceService.resourceFind(resourceService.createResourceQuery(UnixUser.class).propertyEquals(UnixUser.PROPERTY_NAME, name)).orElse(null);
    }

    @Test
    public void testCreating_OK() {

        // User
        UnixUser unixUser = new UnixUser();
        unixUser.setName("the_user");
        unixUser.setPassword("the_password");
        unixUser.setKeepClearPassword(false);

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        UnixUser actual = findUnixUser("the_user");
        Assert.assertNotNull(actual.getId());
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertNull(actual.getPassword());
        Assert.assertFalse(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());

    }

    @Test(expected = IllegalUpdateException.class)
    public void testCreatingLowerId_FAIL() {

        // User
        UnixUser unixUser = new UnixUser();
        unixUser.setId(60000L);
        unixUser.setName("the_user");
        unixUser.setPassword("the_password");
        unixUser.setKeepClearPassword(false);

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

    @Test
    public void testCreatingMaxLength_OK() {

        // User
        UnixUser unixUser = new UnixUser();
        unixUser.setName("a2345678901234567890123456789012");
        unixUser.setPassword("the_password");
        unixUser.setKeepClearPassword(false);

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        UnixUser actual = findUnixUser("a2345678901234567890123456789012");
        Assert.assertNotNull(actual.getId());
        Assert.assertEquals("a2345678901234567890123456789012", actual.getName());
        Assert.assertEquals("/home/a2345678901234567890123456789012", actual.getHomeFolder());
        Assert.assertNull(actual.getPassword());
        Assert.assertFalse(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());

    }

    @Test(expected = IllegalUpdateException.class)
    public void testCreatingNameTooLong_33_FAIL() {

        // User
        UnixUser unixUser = new UnixUser();
        unixUser.setName("a23456789012345678901234567890123");
        unixUser.setPassword("the_password");
        unixUser.setKeepClearPassword(false);

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

    @Test(expected = IllegalUpdateException.class)
    public void testCreatingNameTooLong_34_FAIL() {

        // User
        UnixUser unixUser = new UnixUser();
        unixUser.setName("a234567890123456789012345678901234");
        unixUser.setPassword("the_password");
        unixUser.setKeepClearPassword(false);

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

    @Test(expected = IllegalUpdateException.class)
    public void testCreatingSystemHigherId_FAIL() {

        // User
        SystemUnixUser unixUser = new SystemUnixUser();
        unixUser.setId(71000L);
        unixUser.setName("the_user");
        unixUser.setPassword("the_password");
        unixUser.setKeepClearPassword(false);

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

    @Test
    public void testCreatingSystemLowerId_OK() {

        // User
        SystemUnixUser unixUser = new SystemUnixUser();
        unixUser.setId(0L);
        unixUser.setName("root");
        unixUser.setHomeFolder("/root");

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        UnixUser actual = findUnixUser("root");
        Assert.assertEquals(0L, (long) actual.getId());
        Assert.assertEquals("root", actual.getName());
        Assert.assertEquals("/root", actual.getHomeFolder());
        Assert.assertNull(actual.getPassword());
        Assert.assertFalse(actual.isKeepClearPassword());
        Assert.assertNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());

    }

    @Test
    public void testCreatingWithId_OK() {

        // User
        UnixUser unixUser = new UnixUser();
        unixUser.setId(70120L);
        unixUser.setName("the_user");
        unixUser.setPassword("the_password");
        unixUser.setKeepClearPassword(false);

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        UnixUser actual = findUnixUser("the_user");
        Assert.assertEquals(70120L, (long) actual.getId());
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertNull(actual.getPassword());
        Assert.assertFalse(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());

    }

    @Test(expected = ResourcePrimaryKeyCollisionException.class)
    public void testCreatingWithIdExisting_FAIL() {

        testCreatingWithId_OK();

        // User
        UnixUser unixUser = new UnixUser();
        unixUser.setId(70120L);
        unixUser.setName("the_user_2");

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

    @Test
    public void testCreatingWithKeepPassword() {

        // User
        UnixUser unixUser = new UnixUser();
        unixUser.setName("the_user");
        unixUser.setPassword("the_password");
        unixUser.setKeepClearPassword(true);

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        UnixUser actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertEquals("the_password", actual.getPassword());
        Assert.assertTrue(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());

    }

    @Test
    public void testCreatingWithNoPassword() {

        // User
        UnixUser unixUser = new UnixUser();
        unixUser.setName("the_user");
        unixUser.setKeepClearPassword(true);

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        UnixUser actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertNull(actual.getPassword());
        Assert.assertTrue(actual.isKeepClearPassword());
        Assert.assertNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());

    }

    @Test
    public void testCreatingWithoutKeepPassword() {

        // User
        UnixUser unixUser = new UnixUser();
        unixUser.setName("the_user");
        unixUser.setPassword("the_password");
        unixUser.setKeepClearPassword(false);

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        UnixUser actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertNull(actual.getPassword());
        Assert.assertFalse(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());

    }

    @Test
    public void testUpdatingPassword() {

        // ---------- Create ----------
        // User
        UnixUser unixUser = new UnixUser();
        unixUser.setName("the_user");
        unixUser.setPassword("the_password");
        unixUser.setKeepClearPassword(true);

        // Add
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(unixUser);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        UnixUser actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertEquals("the_password", actual.getPassword());
        Assert.assertTrue(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());

        // ---------- Update with same password (same hash) ----------
        String currentHash = actual.getHashedPassword();
        actual.setPassword("the_password");
        changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceUpdate(actual);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertEquals("the_password", actual.getPassword());
        Assert.assertTrue(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());
        Assert.assertEquals(currentHash, actual.getHashedPassword());

        // ---------- Update with different password ----------
        currentHash = actual.getHashedPassword();
        actual.setPassword("the_password_2");
        changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceUpdate(actual);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertEquals("the_password_2", actual.getPassword());
        Assert.assertTrue(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());
        Assert.assertNotEquals(currentHash, actual.getHashedPassword());

        // ---------- Update without keeping password ----------
        currentHash = actual.getHashedPassword();
        actual.setKeepClearPassword(false);
        changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceUpdate(actual);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertNull(actual.getPassword());
        Assert.assertFalse(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());
        Assert.assertEquals(currentHash, actual.getHashedPassword());

        // ---------- Update with same password (same hash) ----------
        currentHash = actual.getHashedPassword();
        actual.setPassword("the_password_2");
        changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceUpdate(actual);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertNull(actual.getPassword());
        Assert.assertFalse(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());
        Assert.assertEquals(currentHash, actual.getHashedPassword());

        // ---------- Update with different password ----------
        currentHash = actual.getHashedPassword();
        actual.setPassword("the_password");
        changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceUpdate(actual);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        actual = findUnixUser("the_user");
        Assert.assertEquals("the_user", actual.getName());
        Assert.assertEquals("/home/the_user", actual.getHomeFolder());
        Assert.assertNull(actual.getPassword());
        Assert.assertFalse(actual.isKeepClearPassword());
        Assert.assertNotNull(actual.getHashedPassword());
        Assert.assertEquals("/bin/bash", actual.getShell());
        Assert.assertNotEquals(currentHash, actual.getHashedPassword());

        // ---------- Delete ----------
        changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceDelete(actual);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        Assert.assertNull(findUnixUser("the_user"));

    }

}
