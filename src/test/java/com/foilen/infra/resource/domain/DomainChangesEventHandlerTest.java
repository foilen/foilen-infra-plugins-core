/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.domain;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.resource.test.AbstractCorePluginTest;

public class DomainChangesEventHandlerTest extends AbstractCorePluginTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCannotRenameDomain() {

        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        InternalChangeService internalChangeService = getInternalServicesContext().getInternalChangeService();

        // Create one
        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceAdd(new Domain("potato.example.com"));
        internalChangeService.changesExecute(changes);

        // Get it
        Optional<Domain> d = resourceService.resourceFindByPk(new Domain("potato.example.com"));
        Assert.assertTrue(d.isPresent());
        Domain domain = d.get();

        // Rename
        thrown.expectMessage("You cannot rename a Domain. You must delete/add");

        changes = new ChangesContext(resourceService);
        domain.setName("potato2.example.com");
        changes.resourceUpdate(domain);
        internalChangeService.changesExecute(changes);

    }

}
