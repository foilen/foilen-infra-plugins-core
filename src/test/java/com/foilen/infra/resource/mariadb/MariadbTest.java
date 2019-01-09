/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mariadb;

import org.junit.Test;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.resource.test.AbstractCorePluginTest;

public class MariadbTest extends AbstractCorePluginTest {

    @Test(expected = IllegalUpdateException.class)
    public void test_blacklisted_database() {
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(new MariaDBDatabase("mysql", null));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
    }

    @Test(expected = IllegalUpdateException.class)
    public void test_blacklisted_user() {
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(new MariaDBUser("root", null, "qwerty"));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
    }

}
