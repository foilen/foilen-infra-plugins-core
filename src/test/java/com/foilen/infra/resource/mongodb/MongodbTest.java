/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mongodb;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.core.system.junits.ResourcesDump;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.JsonTools;

public class MongodbTest extends AbstractCorePluginTest {

    @Test
    public void test() {
        JunitsHelper.dumpImport(getCommonServicesContext(), getInternalServicesContext(), JsonTools.readFromResource("MongodbTest-test-import.json", ResourcesDump.class, getClass()));

        Application myDb = getCommonServicesContext().getResourceService().resourceFindByPk(new Application("my_db")).get();
        Application myDbManager = getCommonServicesContext().getResourceService().resourceFindByPk(new Application("my_db_manager")).get();

        Map<String, Application> all = new TreeMap<>();
        all.put("myDb", myDb);
        all.put("myDbManager", myDbManager);

        AssertTools.assertJsonComparisonWithoutNulls("MongodbTest-test-expected.json", getClass(), all);
    }

    @Test(expected = IllegalUpdateException.class)
    public void test_blacklisted_database_admin() {
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(new MongoDBDatabase("admin", null));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
    }

    @Test(expected = IllegalUpdateException.class)
    public void test_blacklisted_database_config() {
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(new MongoDBDatabase("config", null));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
    }

    @Test(expected = IllegalUpdateException.class)
    public void test_blacklisted_database_local() {
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(new MongoDBDatabase("local", null));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
    }

    @Test(expected = IllegalUpdateException.class)
    public void test_blacklisted_user() {
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(new MongoDBUser("root", null, "qwerty"));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
    }

}
