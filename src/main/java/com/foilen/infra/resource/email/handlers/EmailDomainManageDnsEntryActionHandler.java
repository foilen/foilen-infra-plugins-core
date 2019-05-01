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
import java.util.Optional;
import java.util.Set;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.dns.DnsEntry;
import com.foilen.infra.resource.dns.DnsPointer;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.infra.resource.email.resources.EmailDomain;
import com.foilen.infra.resource.email.resources.EmailServer;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.base.Strings;

public class EmailDomainManageDnsEntryActionHandler extends AbstractBasics implements ActionHandler {

    private String domainName;

    public EmailDomainManageDnsEntryActionHandler(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing domain {}", domainName);

        IPResourceService resourceService = services.getResourceService();
        Optional<EmailDomain> o = resourceService.resourceFindByPk(new EmailDomain(domainName));
        if (!o.isPresent()) {
            logger.info("{} is not present. Skipping", domainName);
            return;
        }
        EmailDomain emailDomain = o.get();

        // Get the list of machines to point to
        List<Machine> pointToMachines = new ArrayList<>();
        List<EmailServer> emailServers = services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(emailDomain, LinkTypeConstants.INSTALLED_ON, EmailServer.class);
        logger.info("Email Domain {} is installed on server {}", emailDomain, emailServers);
        emailServers.stream() //
                .flatMap(emailServer -> services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(emailServer, LinkTypeConstants.INSTALLED_ON, Machine.class).stream()) //
                .forEach(machine -> {
                    logger.info("On machine {}", machine.getName());
                    pointToMachines.add(machine);
                });

        Set<String> domainNamesWithPointer = new HashSet<>();

        List<DnsEntry> dnsEntries = new ArrayList<>();
        List<DnsPointer> dnsPointers = new ArrayList<>();

        // MX (DnsEntry -> mxDomainName)
        String mxDomainName = emailDomain.getMxDomainName();
        if (!Strings.isNullOrEmpty(mxDomainName) && domainNamesWithPointer.add(mxDomainName)) {
            // DnsEntry
            DnsEntry mxDnsEntry = getOrCreate(resourceService, new DnsEntry(emailDomain.getDomainName(), DnsEntryType.MX, mxDomainName), changes);
            dnsEntries.add(mxDnsEntry);

            // DnsPointer -> POINTS_TO -> machines
            DnsPointer dnsPointer = getOrCreate(resourceService, new DnsPointer(mxDomainName), changes);
            dnsPointers.add(dnsPointer);
            CommonResourceLink.syncToLinks(services, changes, dnsPointer, LinkTypeConstants.POINTS_TO, Machine.class, pointToMachines);
        }

        // imapDomainName DnsPointer
        String imapDomainName = emailDomain.getImapDomainName();
        if (!Strings.isNullOrEmpty(imapDomainName) && domainNamesWithPointer.add(imapDomainName)) {
            DnsPointer dnsPointer = getOrCreate(resourceService, new DnsPointer(imapDomainName), changes);
            dnsPointers.add(dnsPointer);
            CommonResourceLink.syncToLinks(services, changes, dnsPointer, LinkTypeConstants.POINTS_TO, Machine.class, pointToMachines);
        }

        // pop3DomainName DnsPointers
        String pop3DomainName = emailDomain.getPop3DomainName();
        if (!Strings.isNullOrEmpty(pop3DomainName) && domainNamesWithPointer.add(pop3DomainName)) {
            DnsPointer dnsPointer = getOrCreate(resourceService, new DnsPointer(pop3DomainName), changes);
            dnsPointers.add(dnsPointer);
            CommonResourceLink.syncToLinks(services, changes, dnsPointer, LinkTypeConstants.POINTS_TO, Machine.class, pointToMachines);
        }

        CommonResourceLink.syncToLinks(services, changes, emailDomain, LinkTypeConstants.MANAGES, DnsEntry.class, dnsEntries);
        CommonResourceLink.syncToLinks(services, changes, emailDomain, LinkTypeConstants.MANAGES, DnsPointer.class, dnsPointers);

    }

    private DnsEntry getOrCreate(IPResourceService resourceService, DnsEntry dnsEntry, ChangesContext changes) {
        Optional<DnsEntry> o = resourceService.resourceFindByPk(dnsEntry);
        if (o.isPresent()) {
            dnsEntry = o.get();
        } else {
            changes.resourceAdd(dnsEntry);
        }

        return dnsEntry;
    }

    private DnsPointer getOrCreate(IPResourceService resourceService, DnsPointer dnsPointer, ChangesContext changes) {
        Optional<DnsPointer> o = resourceService.resourceFindByPk(dnsPointer);
        if (o.isPresent()) {
            dnsPointer = o.get();
        } else {
            changes.resourceAdd(dnsPointer);
        }

        return dnsPointer;
    }

}
