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
import com.foilen.infra.resource.test.AbstractCorePluginTest;

public class UnixUserChangesEventHandlerTest extends AbstractCorePluginTest {

    private void test_username_FAIL(String name) {
        try {
            test_username_OK(name);
            Assert.fail("Expecting an exception");
        } catch (Exception e) {
        }
    }

    private void test_username_OK(String name) {
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(new UnixUser(null, name, "/home/" + name, null));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
    }

    @Test
    public void test_username_toolong_FAIL() {
        test_username_FAIL("123456789012345678901234567890123");
    }

    @Test
    public void test_username_with_dashes_OK() {
        test_username_OK("the-user");
    }

    @Test
    public void test_username_with_underscore_OK() {
        test_username_OK("the_user");
    }

    @Test
    public void test_username_withDot_FAIL() {
        test_username_FAIL("the.user");
    }

    @Test
    public void test_username_withPlus_FAIL() {
        test_username_FAIL("the+user");
    }

    @Test
    public void test_username_withSpace_FAIL() {
        test_username_FAIL("the user");
    }

}
