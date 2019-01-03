/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mariadb.mysqlmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.foilen.smalltools.tools.AbstractBasics;

public class MysqlManagerConfigDatabaseGrants extends AbstractBasics {

    private String databaseName;
    private List<String> grants = new ArrayList<>();

    public MysqlManagerConfigDatabaseGrants() {
    }

    public MysqlManagerConfigDatabaseGrants(String databaseName, String... grants) {
        this.databaseName = databaseName;
        this.grants.addAll(Arrays.asList(grants));
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public List<String> getGrants() {
        return grants;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setGrants(List<String> grants) {
        this.grants = grants;
    }

}
