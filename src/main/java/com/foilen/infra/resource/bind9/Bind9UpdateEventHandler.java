/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9;

import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractCommonMethodUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.CommonMethodUpdateEventHandlerContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.bind9.service.Bind9Service;
import com.foilen.infra.resource.bind9.service.Bind9ServiceImpl;
import com.foilen.infra.resource.dns.DnsEntry;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;

public class Bind9UpdateEventHandler extends AbstractCommonMethodUpdateEventHandler<Bind9Server> {

    private Bind9Service bind9Service = new Bind9ServiceImpl();

    @Override
    protected void commonHandlerExecute(CommonServicesContext services, ChangesContext changes, CommonMethodUpdateEventHandlerContext<Bind9Server> context) {

        context.setManagedResourcesUpdateContentIfExists(true);

        context.addManagedResourceTypes(Application.class);

        IPResourceService resourceService = services.getResourceService();

        Bind9Server resource = context.getResource();

        // Get the links
        List<Machine> machines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(resource, LinkTypeConstants.INSTALLED_ON, Machine.class);
        List<UnixUser> unixUsers = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(resource, LinkTypeConstants.RUN_AS, UnixUser.class);
        List<DnsEntry> dnsEntries = resourceService.resourceFindAll(resourceService.createResourceQuery(DnsEntry.class));

        // Validate links
        boolean proceed = true;
        if (machines.isEmpty()) {
            logger.info("No machine to install on. Skipping");
            proceed = false;
        }
        if (unixUsers.isEmpty()) {
            logger.info("No unix user to run as. Skipping");
            proceed = false;
        }
        if (unixUsers.size() > 1) {
            logger.warn("Too many unix user to run as");
            throw new IllegalUpdateException("Must have a singe unix user to run as. Got " + unixUsers.size());
        }
        if (dnsEntries.isEmpty()) {
            logger.info("No dns entries on the system. Skipping");
            proceed = false;
        }
        if (resource.getNsDomainNames().isEmpty()) {
            logger.info("No NS domain names set. Skipping");
            proceed = false;
        }

        if (proceed) {

            logger.debug("DnsEntries ; amount {}", dnsEntries.size());
            dnsEntries.forEach(it -> {
                logger.debug("\t{}", it);
            });

            UnixUser unixUser = unixUsers.get(0);

            // Create a Bind9 Application
            Application application = new Application();
            application.setName(resource.getName() + "_bind9");
            application.setDescription("Bind9 Server");
            application.setDomainNames(resource.getNsDomainNames());
            String mainHostName = resource.getNsDomainNames().stream().sorted().findFirst().get();
            String dnsAdminEmail = resource.getAdminEmail();
            int bind9Port = resource.getPort();

            IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
            applicationDefinition.setFrom("foilen/fcloud-docker-bind9:9.10.3-001");
            applicationDefinition.addPortExposed(bind9Port, 53000);
            applicationDefinition.addUdpPortExposed(bind9Port, 53000);
            applicationDefinition.addService("bind", "/usr/sbin/named -g");
            applicationDefinition.addContainerUserToChangeId("bind", unixUser.getId());
            applicationDefinition.setRunAs(unixUser.getId());

            IPApplicationDefinitionAssetsBundle dnsConfigAssetsBundle = applicationDefinition.addAssetsBundle();
            bind9Service.createBindFilesFromDnsEntries(mainHostName, dnsAdminEmail, dnsConfigAssetsBundle, dnsEntries, "etc/bind/");
            dnsConfigAssetsBundle.addAssetResource("/etc/bind/named.conf.options", "/com/foilen/infra/resource/bind9/named.conf.options");

            application.setApplicationDefinition(applicationDefinition);
            context.addManagedResources(application);

            // Link machines
            machines.forEach(machine -> {
                changes.linkAdd(application, LinkTypeConstants.INSTALLED_ON, machine);
            });

            // Link unix user
            changes.linkAdd(application, LinkTypeConstants.RUN_AS, unixUser);
        }

    }

    @Override
    public Class<Bind9Server> supportedClass() {
        return Bind9Server.class;
    }

}
