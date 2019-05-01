/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.composableapplication.parts.AttachableMariaDB;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.mariadb.MariaDBServer;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.unixuser.UnixUser;

public class ComposableApplicationTest extends AbstractCorePluginTest {

    @Test
    public void test() {

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());

        UnixUser appUnixUser = new UnixUser(71000L, "my_user", null, null);
        changes.resourceAdd(appUnixUser);
        UnixUser mariadbUnixUser = new UnixUser(71001L, "my_mariadb", null, null);
        changes.resourceAdd(mariadbUnixUser);

        Machine machine1 = new Machine("m1.example.com", "127.0.100.1");
        changes.resourceAdd(machine1);
        Machine machine2 = new Machine("m2.example.com", "127.0.100.2");
        changes.resourceAdd(machine2);

        MariaDBServer mariaDBServer = new MariaDBServer("mariadb", "my db", "qwerty");
        changes.resourceAdd(mariaDBServer);
        changes.linkAdd(mariaDBServer, LinkTypeConstants.RUN_AS, mariadbUnixUser);
        changes.linkAdd(mariaDBServer, LinkTypeConstants.INSTALLED_ON, machine1);

        AttachableMariaDB attachableMariaDB = new AttachableMariaDB();
        attachableMariaDB.setName("mariadb");
        changes.resourceAdd(attachableMariaDB);
        changes.linkAdd(attachableMariaDB, LinkTypeConstants.POINTS_TO, mariaDBServer);

        ComposableApplication composableApplication = new ComposableApplication("my_app");
        changes.resourceAdd(composableApplication);
        changes.linkAdd(composableApplication, LinkTypeConstants.RUN_AS, appUnixUser);
        changes.linkAdd(composableApplication, LinkTypeConstants.INSTALLED_ON, machine1);
        changes.linkAdd(composableApplication, ComposableApplication.LINK_TYPE_ATTACHED, attachableMariaDB);

        // Execute the changes
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ComposableApplicationTest-test-state-1.json", getClass(), true);

        // Change the attached MariaDB machine
        changes.clear();
        changes.linkDelete(mariaDBServer, LinkTypeConstants.INSTALLED_ON, machine1);
        changes.linkAdd(mariaDBServer, LinkTypeConstants.INSTALLED_ON, machine2);

        // Execute the changes
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ComposableApplicationTest-test-state-2.json", getClass(), true);

    }

}
