/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.mariadb;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.UpdateEventHandler;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tuple.Tuple3;

public class MariaDBUserUpdateHandler extends AbstractBasics implements UpdateEventHandler<MariaDBUser> {

    private static final List<String> blacklisted = Collections.unmodifiableList(Arrays.asList( //
            "root" //
    ));

    @Override
    public void addHandler(CommonServicesContext services, ChangesContext changes, MariaDBUser resource) {
        validate(resource);
    }

    @Override
    public void checkAndFix(CommonServicesContext services, ChangesContext changes, MariaDBUser resource) {
    }

    @Override
    public void deleteHandler(CommonServicesContext services, ChangesContext changes, MariaDBUser resource, List<Tuple3<IPResource, String, IPResource>> previousLinks) {

    }

    @Override
    public Class<MariaDBUser> supportedClass() {
        return MariaDBUser.class;
    }

    @Override
    public void updateHandler(CommonServicesContext services, ChangesContext changes, MariaDBUser previousResource, MariaDBUser newResource) {
        validate(newResource);
    }

    private void validate(MariaDBUser resource) {
        if (blacklisted.contains(resource.getName().toLowerCase())) {
            throw new IllegalUpdateException("That user name is blacklisted");
        }
    }

}
