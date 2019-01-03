/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractCommonMethodUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.CommonMethodUpdateEventHandlerContext;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.dns.DnsEntry;
import com.foilen.infra.resource.dns.DnsPointer;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.infra.resource.email.resources.EmailDomain;
import com.foilen.infra.resource.email.resources.EmailServer;
import com.foilen.infra.resource.machine.Machine;
import com.google.common.base.Strings;

public class EmailDomainManageDnsEntryEventHandler extends AbstractCommonMethodUpdateEventHandler<EmailDomain> {

    @Override
    protected void commonHandlerExecute(CommonServicesContext services, ChangesContext changes, CommonMethodUpdateEventHandlerContext<EmailDomain> context) {

        EmailDomain emailDomain = context.getResource();

        context.addManagedResourceTypes(DnsEntry.class, DnsPointer.class);

        // Get the list of machines to point to
        List<Machine> pointToMachines = new ArrayList<>();
        List<EmailServer> emailServers = services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(emailDomain, LinkTypeConstants.INSTALLED_ON, EmailServer.class);
        logger.debug("Email Domain {} is installed on server {}", emailDomain, emailServers);
        emailServers.stream() //
                .flatMap(emailServer -> services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(emailServer, LinkTypeConstants.INSTALLED_ON, Machine.class).stream()) //
                .forEach(machine -> {
                    logger.debug("On machine {}", machine.getName());
                    pointToMachines.add(machine);
                });

        Set<String> domainNamesWithPointer = new HashSet<>();

        // MX (DnsEntry -> mxDomainName)
        String mxDomainName = emailDomain.getMxDomainName();
        if (!Strings.isNullOrEmpty(mxDomainName) && domainNamesWithPointer.add(mxDomainName)) {
            // DnsEntry
            DnsEntry mxDnsEntry = new DnsEntry(emailDomain.getDomainName(), DnsEntryType.MX, mxDomainName);
            context.addManagedResources(mxDnsEntry);

            // DnsPointer -> POINTS_TO -> machines
            DnsPointer dnsPointer = new DnsPointer(mxDomainName);
            context.addManagedResources(dnsPointer);
            pointToMachines.forEach(machine -> changes.linkAdd(dnsPointer, LinkTypeConstants.POINTS_TO, machine));
        }

        // imapDomainName DnsPointer
        String imapDomainName = emailDomain.getImapDomainName();
        if (!Strings.isNullOrEmpty(imapDomainName) && domainNamesWithPointer.add(imapDomainName)) {
            DnsPointer dnsPointer = new DnsPointer(imapDomainName);
            context.addManagedResources(dnsPointer);
            pointToMachines.forEach(machine -> changes.linkAdd(dnsPointer, LinkTypeConstants.POINTS_TO, machine));
        }

        // pop3DomainName DnsPointers
        String pop3DomainName = emailDomain.getPop3DomainName();
        if (!Strings.isNullOrEmpty(pop3DomainName) && domainNamesWithPointer.add(pop3DomainName)) {
            DnsPointer dnsPointer = new DnsPointer(pop3DomainName);
            context.addManagedResources(dnsPointer);
            pointToMachines.forEach(machine -> changes.linkAdd(dnsPointer, LinkTypeConstants.POINTS_TO, machine));
        }

    }

    @Override
    public Class<EmailDomain> supportedClass() {
        return EmailDomain.class;
    }

}
