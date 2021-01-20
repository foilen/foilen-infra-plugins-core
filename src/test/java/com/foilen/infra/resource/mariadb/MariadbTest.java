/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mariadb;

import org.junit.Test;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.smalltools.tools.JsonTools;

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

    @Test
    public void test_manySameDbName_onDifferentServers_OK() {
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());

        MariaDBUser user1 = new MariaDBUser("wp");
        MariaDBUser user2 = new MariaDBUser("wp");
        changes.resourceAdd(user1);
        changes.resourceAdd(user2);

        MariaDBDatabase database1 = new MariaDBDatabase("wordpress");
        MariaDBDatabase database2 = new MariaDBDatabase("wordpress");
        changes.resourceAdd(database1);
        changes.resourceAdd(database2);

        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_ADMIN, database1);
        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_READ, database1);
        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_WRITE, database1);
        changes.linkAdd(user2, MariaDBUser.LINK_TYPE_ADMIN, database2);
        changes.linkAdd(user2, MariaDBUser.LINK_TYPE_READ, database2);
        changes.linkAdd(user2, MariaDBUser.LINK_TYPE_WRITE, database2);

        MariaDBServer server1 = new MariaDBServer("server1");
        MariaDBServer server2 = new MariaDBServer("server2");
        changes.resourceAdd(server1);
        changes.resourceAdd(server2);

        changes.linkAdd(database1, LinkTypeConstants.INSTALLED_ON, server1);
        changes.linkAdd(database2, LinkTypeConstants.INSTALLED_ON, server2);

        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
    }

    @Test(expected = IllegalUpdateException.class)
    public void test_manySameDbName_onSameServer_FAIL() {
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());

        MariaDBUser user1 = new MariaDBUser("wp");
        MariaDBUser user2 = new MariaDBUser("wp");
        changes.resourceAdd(user1);
        changes.resourceAdd(user2);

        MariaDBDatabase database1 = new MariaDBDatabase("wordpress");
        MariaDBDatabase database2 = new MariaDBDatabase("wordpress");
        changes.resourceAdd(database1);
        changes.resourceAdd(database2);

        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_ADMIN, database1);
        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_READ, database1);
        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_WRITE, database1);
        changes.linkAdd(user2, MariaDBUser.LINK_TYPE_ADMIN, database2);
        changes.linkAdd(user2, MariaDBUser.LINK_TYPE_READ, database2);
        changes.linkAdd(user2, MariaDBUser.LINK_TYPE_WRITE, database2);

        MariaDBServer server1 = new MariaDBServer("server1");
        changes.resourceAdd(server1);

        changes.linkAdd(database1, LinkTypeConstants.INSTALLED_ON, server1);
        changes.linkAdd(database2, LinkTypeConstants.INSTALLED_ON, server1);

        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
    }

    @Test(expected = IllegalUpdateException.class)
    public void test_manySameUserName_usersOnSameDb_OK() {
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());

        MariaDBUser user1 = new MariaDBUser("wp");
        MariaDBUser user2 = new MariaDBUser("wp");
        changes.resourceAdd(user1);
        changes.resourceAdd(user2);

        MariaDBDatabase database1 = new MariaDBDatabase("wordpress");
        changes.resourceAdd(database1);

        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_ADMIN, database1);
        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_READ, database1);
        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_WRITE, database1);
        changes.linkAdd(user2, MariaDBUser.LINK_TYPE_ADMIN, database1);

        MariaDBServer server1 = new MariaDBServer("server1");
        changes.resourceAdd(server1);

        changes.linkAdd(database1, LinkTypeConstants.INSTALLED_ON, server1);

        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
    }

    @Test(expected = IllegalUpdateException.class)
    public void test_renaming_2_FAIL() {
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());

        MariaDBUser user1 = new MariaDBUser("wp");
        changes.resourceAdd(user1);

        MariaDBDatabase database1 = new MariaDBDatabase("wordpress");
        changes.resourceAdd(database1);

        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_ADMIN, database1);
        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_READ, database1);
        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_WRITE, database1);

        MariaDBServer server1 = new MariaDBServer("server1");
        changes.resourceAdd(server1);

        changes.linkAdd(database1, LinkTypeConstants.INSTALLED_ON, server1);

        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Do the renaming
        changes.clear();
        server1 = getCommonServicesContext().getResourceService().resourceFindByPk(server1).get();
        server1.setName("wordpress_newname");
        changes.resourceUpdate(server1);

        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

    @Test(expected = IllegalUpdateException.class)
    public void test_renaming_FAIL() {
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());

        MariaDBUser user1 = new MariaDBUser("wp");
        changes.resourceAdd(user1);

        MariaDBDatabase database1 = new MariaDBDatabase("wordpress");
        changes.resourceAdd(database1);

        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_ADMIN, database1);
        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_READ, database1);
        changes.linkAdd(user1, MariaDBUser.LINK_TYPE_WRITE, database1);

        MariaDBServer server1 = new MariaDBServer("server1");
        changes.resourceAdd(server1);

        changes.linkAdd(database1, LinkTypeConstants.INSTALLED_ON, server1);

        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Do the renaming
        changes.clear();
        server1 = getCommonServicesContext().getResourceService().resourceFindByPk(server1).get();
        MariaDBServer renamed = JsonTools.clone(server1);
        renamed.setName("wordpress_newname");
        changes.resourceUpdate(server1, renamed);

        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

}
