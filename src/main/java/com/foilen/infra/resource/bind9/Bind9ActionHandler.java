/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.bind9.service.Bind9Service;
import com.foilen.infra.resource.bind9.service.Bind9ServiceImpl;
import com.foilen.infra.resource.dns.DnsEntry;
import com.foilen.infra.resource.domain.DomainResourceHelper;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.utils.ActionsHandlerUtils;
import com.foilen.smalltools.tools.AbstractBasics;

public class Bind9ActionHandler extends AbstractBasics implements ActionHandler {

    private static final Bind9Service bind9Service = new Bind9ServiceImpl();

    private String bind9ServerName;

    public Bind9ActionHandler(String bind9ServerName) {
        this.bind9ServerName = bind9ServerName;
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing bind9Server {}", bind9ServerName);

        IPResourceService resourceService = services.getResourceService();
        Optional<Bind9Server> o = resourceService.resourceFindByPk(new Bind9Server(bind9ServerName));
        if (!o.isPresent()) {
            logger.info("{} is not present. Skipping", bind9ServerName);
            return;
        }
        Bind9Server bind9Server = o.get();

        // Get the links
        List<Machine> machines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(bind9Server, LinkTypeConstants.INSTALLED_ON, Machine.class);
        List<UnixUser> unixUsers = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(bind9Server, LinkTypeConstants.RUN_AS, UnixUser.class);
        List<DnsEntry> dnsEntries = resourceService.resourceFindAll(resourceService.createResourceQuery(DnsEntry.class));

        List<Application> desiredManageApplications = new ArrayList<>();

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
        if (bind9Server.getNsDomainNames().isEmpty()) {
            logger.info("No NS domain names set. Skipping");
            proceed = false;
        }

        if (proceed) {

            logger.info("DnsEntries ; amount {}", dnsEntries.size());
            dnsEntries.forEach(it -> {
                logger.debug("\t{}", it);
            });

            UnixUser unixUser = unixUsers.get(0);

            // Create a Bind9 Application
            Application application = ActionsHandlerUtils.getOrCreateAnApplication(resourceService, bind9Server.getName() + "_bind9");
            desiredManageApplications.add(application);
            application.setDescription("Bind9 Server");
            application.setDomainNames(bind9Server.getNsDomainNames());
            String mainHostName = bind9Server.getNsDomainNames().stream().sorted().findFirst().get();
            String dnsAdminEmail = bind9Server.getAdminEmail();
            int bind9Port = bind9Server.getPort();

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

            ActionsHandlerUtils.addOrUpdate(application, changes);

            // Sync links
            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.INSTALLED_ON, Machine.class, machines);
            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.RUN_AS, UnixUser.class, unixUsers);
            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.USES, DnsEntry.class, dnsEntries);

        }

        CommonResourceLink.syncToLinks(services, changes, bind9Server, LinkTypeConstants.MANAGES, Application.class, desiredManageApplications);

        // Sync managed domain names
        DomainResourceHelper.syncManagedLinks(services, changes, bind9Server, bind9Server.getNsDomainNames());

    }

}
