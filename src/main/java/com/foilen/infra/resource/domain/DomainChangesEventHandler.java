/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerResourceStream;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StringTools;

public class DomainChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        // TODO + Junit cannot rename a domain
        // Validate cannot rename a domain
        changesInTransactionContext.getLastUpdatedResources().stream() //
                .filter(update -> update.getNext() instanceof Domain) //
                .forEach(update -> {
                    Domain previous = (Domain) update.getPrevious();
                    Domain next = (Domain) update.getNext();
                    if (!StringTools.safeEquals(previous.getName(), next.getName())) {
                        throw new IllegalUpdateException("You cannot rename a Domain. You must delete/add");
                    }
                });

        // Ensure attached to its parent domain (create it if missing)
        List<ActionHandler> actions = new ArrayList<>();
        new ChangesEventHandlerResourceStream<>(Domain.class) //
                .resourcesAddOfType(changesInTransactionContext.getLastAddedResources()) //
                .resourcesAddOfType(changesInTransactionContext.getLastRefreshedResources()) //
                .getResourcesStream() //
                .forEach(domain -> {
                    logger.info("Ensure {} has an attached parent", domain.getName());

                    Optional<Domain> parentOptional = DomainResourceHelper.getParent(domain);
                    if (parentOptional.isEmpty()) {
                        return;
                    }

                    actions.add((s, changes) -> {

                        // Create the parent if does not exists
                        Domain parent = parentOptional.get();
                        Optional<Domain> existingParentOptional = s.getResourceService().resourceFindByPk(parent);
                        if (existingParentOptional.isPresent()) {
                            logger.info("Parent of {} -> {} already exists", domain.getName(), parent.getName());
                            parent = existingParentOptional.get();
                        } else {
                            logger.info("Parent of {} -> {} does not exist. Adding it", domain.getName(), parent.getName());
                            changes.resourceAdd(parent);
                        }

                        // Add link
                        changes.linkAdd(parent, Domain.LINK_TYPE_SUBDOMAIN, domain);

                    });
                });

        return actions;
    }

}
