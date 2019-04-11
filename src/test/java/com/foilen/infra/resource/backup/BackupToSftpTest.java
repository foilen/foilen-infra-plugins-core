/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.backup;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.resource.backup.resources.BackupToSftpConfig;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.test.AbstractCorePluginTest;

public class BackupToSftpTest extends AbstractCorePluginTest {

    @Test
    public void test_basic() {

        // Add basic resources
        BackupToSftpConfig backupToSftpConfig = new BackupToSftpConfig();
        backupToSftpConfig.setUid("abc");
        backupToSftpConfig.setSshHostname("backup.example.com");
        backupToSftpConfig.setSshUser("backup");
        backupToSftpConfig.setSshPrivateKey("KEY");
        backupToSftpConfig.setRemotePath("/home/backup");

        Machine f1 = new Machine("f1.example.com", "127.0.0.1");
        Machine f2 = new Machine("f2.example.com", "127.0.0.2");

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(backupToSftpConfig);
        changes.resourceAdd(f1);
        changes.resourceAdd(f2);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "BackupToSftpTest-state-test_basic-1.json", getClass(), true);

        // Remove f2
        changes.resourceDelete(f2);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "BackupToSftpTest-state-test_basic-2.json", getClass(), true);

        // Remove f1
        changes.resourceDelete(f1);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "BackupToSftpTest-state-test_basic-3.json", getClass(), true);

    }

    @Test
    public void test_create_later() {

        // Add basic resources
        BackupToSftpConfig backupToSftpConfig = new BackupToSftpConfig();
        backupToSftpConfig.setUid("abc");
        backupToSftpConfig.setSshHostname("backup.example.com");
        backupToSftpConfig.setSshUser("backup");
        backupToSftpConfig.setSshPrivateKey("KEY");
        backupToSftpConfig.setRemotePath("/home/backup");

        Machine f1 = new Machine("f1.example.com", "127.0.0.1");
        Machine f2 = new Machine("f2.example.com", "127.0.0.2");

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(f1);
        changes.resourceAdd(f2);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
        changes.resourceAdd(backupToSftpConfig);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "BackupToSftpTest-state-test_create_later-1.json", getClass(), true);

        // Remove config
        changes.resourceDelete(backupToSftpConfig);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "BackupToSftpTest-state-test_create_later-2.json", getClass(), true);

    }

}
