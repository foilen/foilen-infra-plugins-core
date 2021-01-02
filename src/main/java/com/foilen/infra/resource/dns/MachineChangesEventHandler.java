/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import java.util.ArrayList;
import java.util.List;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StringTools;
import com.google.common.base.Strings;

public class MachineChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {
        List<ActionHandler> actions = new ArrayList<ActionHandler>();

        // Added
        ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), Machine.class) //
                .forEach(machine -> {
                    actions.add((s, changes) -> {

                        // Use a DnsEntry
                        if (machine.getPublicIp() == null) {
                            logger.info("{} was added. Not creating DnsEntry", machine);
                        } else {
                            logger.info("{} was added. Create DnsEntry", machine);
                            IPResource dnsEntry = new DnsEntry(machine.getName(), DnsEntryType.A, machine.getPublicIp());
                            changes.resourceAdd(dnsEntry);
                            changes.linkAdd(machine, LinkTypeConstants.MANAGES, dnsEntry);
                        }

                    });
                });

        // Updated
        ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), Machine.class) //
                .forEach(previousAndNew -> {
                    actions.add((s, changes) -> {

                        Machine previousMachine = (Machine) previousAndNew.getPrevious();
                        Machine nextMachine = (Machine) previousAndNew.getNext();

                        logger.info("{} changed to {}", previousMachine, nextMachine);

                        // Validate no name change
                        if (!StringTools.safeEquals(previousMachine.getName(), nextMachine.getName())) {
                            throw new IllegalUpdateException("You cannot change a Machine's name");
                        }

                        // Use a DnsEntry
                        List<DnsEntry> desiredDnsEntries = new ArrayList<>();
                        if (!Strings.isNullOrEmpty(nextMachine.getPublicIp())) {
                            desiredDnsEntries.add(new DnsEntry(nextMachine.getName(), DnsEntryType.A, nextMachine.getPublicIp()));
                        }
                        // Create new DnsEntry
                        desiredDnsEntries.forEach(dnsEntry -> {
                            if (!s.getResourceService().resourceFindByPk(dnsEntry).isPresent()) {
                                changes.resourceAdd(dnsEntry);
                            }
                        });

                        CommonResourceLink.syncToLinks(services, changes, nextMachine, LinkTypeConstants.MANAGES, DnsEntry.class, desiredDnsEntries);

                    });
                });

        // Refreshed
        ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastRefreshedResources(), Machine.class) //
                .forEach(machine -> {
                    actions.add((s, changes) -> {

                        logger.info("{} refresh {}", machine);

                        // Use a DnsEntry
                        List<DnsEntry> desiredDnsEntries = new ArrayList<>();
                        if (!Strings.isNullOrEmpty(machine.getPublicIp())) {
                            desiredDnsEntries.add(new DnsEntry(machine.getName(), DnsEntryType.A, machine.getPublicIp()));
                        }
                        // Create new DnsEntry
                        desiredDnsEntries.forEach(dnsEntry -> {
                            if (!s.getResourceService().resourceFindByPk(dnsEntry).isPresent()) {
                                changes.resourceAdd(dnsEntry);
                            }
                        });

                        CommonResourceLink.syncToLinks(services, changes, machine, LinkTypeConstants.MANAGES, DnsEntry.class, desiredDnsEntries);

                    });
                });

        // Removed: Nothing to do

        return actions;
    }

}
