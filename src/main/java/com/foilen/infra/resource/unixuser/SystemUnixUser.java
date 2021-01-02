/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.unixuser;

/**
 * Like UnixUser, but with ids lower than 70000. (They are not being installed on the machines, but should already exists)
 */
public class SystemUnixUser extends UnixUser {

    public static final String RESOURCE_TYPE = "System Unix User";

    public SystemUnixUser() {
    }

    public SystemUnixUser(Long id, String name) {
        setId(id);
        setName(name);
    }
}
