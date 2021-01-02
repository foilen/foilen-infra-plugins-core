/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.domain;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.resource.test.AbstractCorePluginTest;

public class DomainChangesEventHandlerTest extends AbstractCorePluginTest {

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
        changes.clear();
        domain.setName("potato2.example.com");
        changes.resourceUpdate(domain);
        Assert.assertThrows("You cannot rename a Domain. You must delete/add", IllegalUpdateException.class, () -> {
            internalChangeService.changesExecute(changes);
        });

    }

}
