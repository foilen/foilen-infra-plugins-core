/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mariadb.mysqlmanager;

import java.util.ArrayList;
import java.util.List;

import com.foilen.smalltools.tools.AbstractBasics;

public class MysqlManagerConfig extends AbstractBasics {

    private MysqlManagerConfigAdmin admin = new MysqlManagerConfigAdmin();
    private List<String> databases = new ArrayList<>();
    private List<MysqlManagerConfigUser> usersToIgnore = new ArrayList<>();
    private List<MysqlManagerConfigPermission> usersPermissions = new ArrayList<>();

    public MysqlManagerConfigAdmin getAdmin() {
        return admin;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public List<MysqlManagerConfigPermission> getUsersPermissions() {
        return usersPermissions;
    }

    public List<MysqlManagerConfigUser> getUsersToIgnore() {
        return usersToIgnore;
    }

    public void setAdmin(MysqlManagerConfigAdmin admin) {
        this.admin = admin;
    }

    public void setDatabases(List<String> databases) {
        this.databases = databases;
    }

    public void setUsersPermissions(List<MysqlManagerConfigPermission> usersPermissions) {
        this.usersPermissions = usersPermissions;
    }

    public void setUsersToIgnore(List<MysqlManagerConfigUser> usersToIgnore) {
        this.usersToIgnore = usersToIgnore;
    }

}
