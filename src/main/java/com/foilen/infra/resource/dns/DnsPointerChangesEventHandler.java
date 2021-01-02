/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.infra.resource.domain.DomainResourceHelper;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.base.Strings;

public class DnsPointerChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        // DnsPointer is added or updated
        Set<DnsPointer> dnsPointersToRefresh = new HashSet<>();

        dnsPointersToRefresh.addAll(ChangesEventHandlerUtils.getResourcesOfType(changesInTransactionContext.getLastAddedResources(), DnsPointer.class));
        dnsPointersToRefresh.addAll(ChangesEventHandlerUtils.getResourcesOfType(changesInTransactionContext.getLastRefreshedResources(), DnsPointer.class));
        dnsPointersToRefresh.addAll(ChangesEventHandlerUtils.getNextResourcesOfType(changesInTransactionContext.getLastUpdatedResources(), DnsPointer.class));

        // DnsPointer link pointing to a Machine is added or removed
        Set<Machine> machinesToRefresh = new HashSet<>();
        dnsPointersToRefresh.addAll(ChangesEventHandlerUtils.getFromResources(changesInTransactionContext.getLastAddedLinks(), DnsPointer.class, LinkTypeConstants.POINTS_TO, Machine.class));
        dnsPointersToRefresh.addAll(ChangesEventHandlerUtils.getFromResources(changesInTransactionContext.getLastDeletedLinks(), DnsPointer.class, LinkTypeConstants.POINTS_TO, Machine.class));
        machinesToRefresh.addAll(ChangesEventHandlerUtils.getToResources(changesInTransactionContext.getLastAddedLinks(), DnsPointer.class, LinkTypeConstants.POINTS_TO, Machine.class));
        machinesToRefresh.addAll(ChangesEventHandlerUtils.getToResources(changesInTransactionContext.getLastDeletedLinks(), DnsPointer.class, LinkTypeConstants.POINTS_TO, Machine.class));

        // Machine is updated and is pointed by a DnsPointer
        ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), Machine.class) //
                .forEach(pAndN -> {
                    Machine machine = (Machine) pAndN.getNext();
                    if (machinesToRefresh.add(machine)) {
                        dnsPointersToRefresh.addAll(services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(DnsPointer.class, LinkTypeConstants.POINTS_TO, machine));
                    }
                });

        // Update the DnsPointers
        List<ActionHandler> actions = new ArrayList<>();

        dnsPointersToRefresh.stream().map(it -> it.getName()).forEach(dnsPointerName -> {

            actions.add((s, changes) -> {
                logger.info("Processing {}", dnsPointerName);

                IPResourceService resourceService = services.getResourceService();
                Optional<DnsPointer> o = resourceService.resourceFindByPk(new DnsPointer(dnsPointerName));
                if (!o.isPresent()) {
                    logger.info("{} is not present. Skipping", dnsPointerName);
                    return;
                }
                DnsPointer dnsPointer = o.get();

                List<Machine> machines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(dnsPointer, LinkTypeConstants.POINTS_TO, Machine.class);
                logger.debug("{} points to {} machines", dnsPointer.getName(), machines.size());

                List<DnsEntry> desiredDnsEntries = machines.stream() //
                        .filter(machine -> !Strings.isNullOrEmpty(machine.getPublicIp())) //
                        .map(machine -> new DnsEntry(dnsPointer.getName(), DnsEntryType.A, machine.getPublicIp())) //
                        .collect(Collectors.toList());

                // Create new DnsEntry
                desiredDnsEntries.forEach(dnsEntry -> {
                    if (!resourceService.resourceFindByPk(dnsEntry).isPresent()) {
                        changes.resourceAdd(dnsEntry);
                    }
                });
                CommonResourceLink.syncToLinks(services, changes, dnsPointer, LinkTypeConstants.MANAGES, DnsEntry.class, desiredDnsEntries);

                // Sync domains
                DomainResourceHelper.syncManagedLinks(s, changes, dnsPointer, dnsPointer.getName());

            });

        });

        return actions;
    }

}
