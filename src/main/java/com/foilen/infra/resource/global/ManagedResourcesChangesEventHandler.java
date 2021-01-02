/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.global;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.smalltools.tools.AbstractBasics;

/**
 * When a Link with MANAGE is deleted, delete the toResource (if no more linked by others).
 *
 * When a resource that is still MANAGEd by other resources is deleted, fail.
 */
public class ManagedResourcesChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        List<ActionHandler> actions = new ArrayList<>();

        // Cannot delete a managed resource unless all its links are removed
        changesInTransactionContext.getLastDeletedResources().forEach(deletedResource -> {

            logger.info("Processing deleted resource {}. Get any Managed links going to it", deletedResource);
            List<? extends IPResource> managingResources = services.getResourceService().linkFindAllByLinkTypeAndToResource(LinkTypeConstants.MANAGES, deletedResource);
            logger.info("Deleted resource {} has {} resources managing it", deletedResource, managingResources.size());
            if (!managingResources.isEmpty()) {
                throw new IllegalUpdateException("You cannot delete the resource " + deletedResource.getResourceName() + " while there are resources managing it");
            }

        });

        // Delete a managed resource when all its managed links are removed
        ChangesEventHandlerUtils.getToResourcesStream(changesInTransactionContext.getLastDeletedLinks(), LinkTypeConstants.MANAGES) //
                .filter(managedResource -> !changesInTransactionContext.getAllDeletedResources().contains(managedResource)) //
                .map(IPResource::getInternalId) //
                .sorted().distinct() //
                .forEach(possibleManagedResourceId -> {

                    actions.add((s, changes) -> {
                        logger.info("Processing managed resource with id {}", possibleManagedResourceId);

                        IPResourceService resourceService = s.getResourceService();
                        Optional<IPResource> managedResourceOptional = resourceService.resourceFind(possibleManagedResourceId);
                        if (!managedResourceOptional.isPresent()) {
                            logger.info("Managed resource with id {} does not exist anymore. Skipping", possibleManagedResourceId);
                            return;
                        }

                        IPResource managedResource = managedResourceOptional.get();

                        List<? extends IPResource> links = resourceService.linkFindAllByLinkTypeAndToResource(LinkTypeConstants.MANAGES, managedResource);
                        if (links.isEmpty()) {
                            logger.info("Managed resource {} does not have links from managers. Deleting", managedResource);
                            changes.resourceDelete(managedResource);
                        } else {
                            logger.info("Managed resource {} still have {} links from managers. Keeping", managedResource, links.size());
                        }

                    });

                });
        ;

        return actions;
    }

}
