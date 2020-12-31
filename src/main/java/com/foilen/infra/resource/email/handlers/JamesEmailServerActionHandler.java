/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.AttachablePartContext;
import com.foilen.infra.resource.composableapplication.parts.AttachableMariaDB;
import com.foilen.infra.resource.email.handlers.config.EmailConfig;
import com.foilen.infra.resource.email.handlers.config.EmailConfigDatabase;
import com.foilen.infra.resource.email.handlers.config.EmailConfigDomainAndRelay;
import com.foilen.infra.resource.email.handlers.config.EmailManagerConfig;
import com.foilen.infra.resource.email.handlers.config.EmailManagerConfigAccount;
import com.foilen.infra.resource.email.handlers.config.EmailManagerConfigRedirection;
import com.foilen.infra.resource.email.resources.EmailAccount;
import com.foilen.infra.resource.email.resources.EmailDomain;
import com.foilen.infra.resource.email.resources.EmailRedirection;
import com.foilen.infra.resource.email.resources.EmailRelay;
import com.foilen.infra.resource.email.resources.JamesEmailServer;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.mariadb.MariaDBDatabase;
import com.foilen.infra.resource.mariadb.MariaDBUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.utils.ActionsHandlerUtils;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Strings;

public class JamesEmailServerActionHandler extends AbstractBasics implements ActionHandler {

    private String serverName;

    public JamesEmailServerActionHandler(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing James Server {}", serverName);

        IPResourceService resourceService = services.getResourceService();
        Optional<JamesEmailServer> o = resourceService.resourceFindByPk(new JamesEmailServer(serverName));
        if (!o.isPresent()) {
            logger.info("{} is not present. Skipping", serverName);
            return;
        }
        JamesEmailServer jamesEmailServer = o.get();

        // Get the links
        List<Machine> machines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(jamesEmailServer, LinkTypeConstants.INSTALLED_ON, Machine.class);
        List<UnixUser> unixUsers = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(jamesEmailServer, LinkTypeConstants.RUN_AS, UnixUser.class);

        List<MariaDBDatabase> mariadbDatabases = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(jamesEmailServer, LinkTypeConstants.USES, MariaDBDatabase.class);
        List<MariaDBUser> mariadbUsers = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(jamesEmailServer, LinkTypeConstants.USES, MariaDBUser.class);

        List<WebsiteCertificate> certsSmtp = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(jamesEmailServer, "USES_SMTP", WebsiteCertificate.class);
        List<WebsiteCertificate> certsImap = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(jamesEmailServer, "USES_IMAP", WebsiteCertificate.class);
        List<WebsiteCertificate> certsPop3 = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(jamesEmailServer, "USES_POP3", WebsiteCertificate.class);

        List<AttachablePart> attachedParts = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(jamesEmailServer, "ATTACHED", AttachablePart.class);

        List<AttachableMariaDB> attachableMariaDB = attachedParts.stream() //
                .filter(it -> it instanceof AttachableMariaDB) //
                .map(it -> (AttachableMariaDB) it) //
                .collect(Collectors.toList());

        List<Application> desiredManagedApplications = new ArrayList<>();

        // Validate links
        boolean proceed = true;
        if (machines.isEmpty()) {
            logger.info("No machine to install on. Skipping");
            proceed = false;
        }
        if (unixUsers.size() > 1) {
            logger.warn("Too many unix user to run as");
            throw new IllegalUpdateException("Must have a singe unix user to run as. Got " + unixUsers.size());
        }
        if (unixUsers.isEmpty()) {
            logger.info("No unix user to run as. Skipping");
            proceed = false;
        }
        if (attachableMariaDB.size() > 1) {
            logger.warn("Too many MariaDB Server to use");
            throw new IllegalUpdateException("Must have a singe MariaDB Server to use. Got " + attachableMariaDB.size());
        }
        if (attachableMariaDB.isEmpty()) {
            logger.info("No MariaDB Server to use. Skipping");
            proceed = false;
        }
        if (mariadbDatabases.size() > 1) {
            logger.warn("Too many MariaDB Database to use");
            throw new IllegalUpdateException("Must have a singe MariaDB Database to use. Got " + mariadbDatabases.size());
        }
        if (mariadbDatabases.isEmpty()) {
            logger.info("No MariaDB Database to use. Skipping");
            proceed = false;
        }
        if (mariadbUsers.size() > 1) {
            logger.warn("Too many MariaDB User to use");
            throw new IllegalUpdateException("Must have a singe MariaDB User to use. Got " + mariadbUsers.size());
        }
        if (mariadbUsers.isEmpty()) {
            logger.info("No MariaDB User to use. Skipping");
            proceed = false;
        }

        if (certsSmtp.size() > 1) {
            logger.warn("Too many SMTP Certs to use");
            throw new IllegalUpdateException("Must have a singe SMTP Certs to use. Got " + certsSmtp.size());
        }
        if (certsSmtp.isEmpty()) {
            logger.info("No SMTP Certs to use. Skipping");
            proceed = false;
        }
        if (certsImap.size() > 1) {
            logger.warn("Too many IMAP Certs to use");
            throw new IllegalUpdateException("Must have a singe IMAP Certs to use. Got " + certsImap.size());
        }
        if (certsImap.isEmpty()) {
            logger.info("No IMAP Certs to use. Skipping");
            proceed = false;
        }
        if (certsPop3.size() > 1) {
            logger.warn("Too many POP3 Certs to use");
            throw new IllegalUpdateException("Must have a singe POP3 Certs to use. Got " + certsPop3.size());
        }
        if (certsPop3.isEmpty()) {
            logger.info("No POP3 Certs to use. Skipping");
            proceed = false;
        }

        if (proceed) {

            UnixUser unixUser = unixUsers.get(0);
            MariaDBDatabase mariadbDatabase = mariadbDatabases.get(0);
            MariaDBUser mariadbUser = mariadbUsers.get(0);

            // Create an Application
            Application application = ActionsHandlerUtils.getOrCreateAnApplication(resourceService, jamesEmailServer.getName());
            desiredManagedApplications.add(application);
            application.setDescription(jamesEmailServer.getResourceDescription());

            IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
            application.setApplicationDefinition(applicationDefinition);
            applicationDefinition.setFrom("foilen/foilen-email-server:" + jamesEmailServer.getVersion());

            // Command
            String command = "/app/bin/foilen-email-server --jamesConfigFile /jamesConfig.json --managerConfigFile /emailManagerConfig.json --workDir /workDir";
            if (jamesEmailServer.isEnableDebuglogs()) {
                command += " --debug";
            }
            applicationDefinition.setCommand(command);

            // Config files
            EmailConfig emailConfig = new EmailConfig();
            emailConfig.setDatabase(new EmailConfigDatabase() //
                    .setHostname("127.0.0.1") //
                    .setDatabase(mariadbDatabase.getName()) //
                    .setUsername(mariadbUser.getName()) //
                    .setPassword(mariadbUser.getPassword()) //
            );
            emailConfig.setPostmasterEmail(jamesEmailServer.getPostmasterEmail());
            emailConfig.setEnableDebugDumpMessagesDetails(jamesEmailServer.isEnableDebugDumpMessagesDetails());
            emailConfig.setDisableRelayDeniedNotifyPostmaster(jamesEmailServer.isDisableRelayDeniedNotifyPostmaster());
            emailConfig.setDisableBounceNotifyPostmaster(jamesEmailServer.isDisableBounceNotifyPostmaster());
            emailConfig.setDisableBounceNotifySender(jamesEmailServer.isDisableBounceNotifySender());

            // Certificates
            emailConfig.setImapCertPemFile("/cert-imap.pem");
            emailConfig.setPop3CertPemFile("/cert-pop3.pem");
            emailConfig.setSmtpCertPemFile("/cert-smtp.pem");

            IPApplicationDefinitionAssetsBundle assetsBundle = applicationDefinition.addAssetsBundle();
            assetsBundle.addAssetContent("/cert-imap.pem", toPem(certsImap.get(0)));
            assetsBundle.addAssetContent("/cert-pop3.pem", toPem(certsPop3.get(0)));
            assetsBundle.addAssetContent("/cert-smtp.pem", toPem(certsSmtp.get(0)));

            // Config for Manager Daemon Service
            EmailManagerConfig emailManagerConfig = new EmailManagerConfig();
            List<EmailDomain> emailDomains = resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(EmailDomain.class, LinkTypeConstants.INSTALLED_ON, jamesEmailServer);
            List<Tuple2<String, EmailRelay>> domainAndRelais = new ArrayList<>();
            emailDomains.forEach(emailDomain -> {
                emailManagerConfig.getDomains().add(emailDomain.getDomainName());

                resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(EmailAccount.class, LinkTypeConstants.INSTALLED_ON, emailDomain).forEach(emailAccount -> {
                    EmailManagerConfigAccount configAccount = new EmailManagerConfigAccount();
                    configAccount.setEmail(emailAccount.getAccountName() + "@" + emailDomain.getDomainName());
                    configAccount.setPasswordSha512(emailAccount.getSha512Password());
                    emailManagerConfig.getAccounts().add(configAccount);
                });

                resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(EmailRedirection.class, LinkTypeConstants.INSTALLED_ON, emailDomain).forEach(emailRedirection -> {
                    EmailManagerConfigRedirection configRedirection = new EmailManagerConfigRedirection();
                    String accountName = emailRedirection.getAccountName();
                    if (Strings.isNullOrEmpty(accountName)) {
                        accountName = "*";
                    }
                    configRedirection.setEmail(accountName + "@" + emailDomain.getDomainName());
                    configRedirection.setRedirectTos(emailRedirection.getRedirectTos().stream().sorted().collect(Collectors.toList()));
                    emailManagerConfig.getRedirections().add(configRedirection);
                });

                List<EmailRelay> emailRelais = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(emailDomain, "SEND_THROUGHT", EmailRelay.class);
                if (!emailRelais.isEmpty()) {
                    domainAndRelais.add(new Tuple2<>(emailDomain.getDomainName(), emailRelais.get(0)));
                }

            });

            // Relay domains
            Collections.sort(domainAndRelais, new Comparator<Tuple2<String, EmailRelay>>() {
                @Override
                public int compare(Tuple2<String, EmailRelay> o1, Tuple2<String, EmailRelay> o2) {
                    return o1.getA().compareTo(o2.getA());
                }
            });

            emailConfig.setDomainAndRelais(domainAndRelais.stream() //
                    .map(it -> new EmailConfigDomainAndRelay() //
                            .setDomain(it.getA()) //
                            .setHostname(it.getB().getHostname()) //
                            .setPort(it.getB().getPort()) //
                            .setUsername(it.getB().getUsername()) //
                            .setPassword(it.getB().getPassword()) //
                    ) //
                    .collect(Collectors.toList()));

            Collections.sort(emailManagerConfig.getDomains());
            Collections.sort(emailManagerConfig.getAccounts());
            Collections.sort(emailManagerConfig.getRedirections());

            assetsBundle.addAssetContent("/jamesConfig.json", JsonTools.prettyPrintWithoutNulls(emailConfig));
            applicationDefinition.addCopyWhenStartedContent("/emailManagerConfig.json", JsonTools.prettyPrintWithoutNulls(emailManagerConfig));

            // Attach parts in a deterministic order
            logger.debug("attachedParts ; amount {}", attachedParts.size());
            attachedParts.stream() //
                    .sorted((a, b) -> a.getResourceName().compareTo(b.getResourceName())) //
                    .forEach(attachedPart -> {
                        logger.debug("Attaching {} with type {}", attachedPart.getResourceName(), attachedPart.getClass().getName());
                        attachedPart.attachTo(new AttachablePartContext().setServices(services).setApplication(application).setApplicationDefinition(applicationDefinition));
                    });

            // Workdir
            if (unixUser.getHomeFolder() != null) {
                applicationDefinition.addVolume(new IPApplicationDefinitionVolume( //
                        unixUser.getHomeFolder() + "/_" + jamesEmailServer.getName(), //
                        "/workDir"));
            }

            // Ports
            applicationDefinition.addPortEndpoint(10025, DockerContainerEndpoints.SMTP_TCP);
            applicationDefinition.addPortEndpoint(10465, "SMTP_TLS_TCP");
            applicationDefinition.addPortEndpoint(10587, "SUBMISSION_TCP");
            applicationDefinition.addPortEndpoint(10110, "POP3_TCP");
            applicationDefinition.addPortEndpoint(10143, "IMAP_TCP");
            applicationDefinition.addPortEndpoint(10993, "IMAP_TLS_TCP");

            if (jamesEmailServer.isExposePorts()) {
                applicationDefinition.addPortExposed(25, 10025); // SMTP with startTLS
                applicationDefinition.addPortExposed(465, 10465); // SMTP with TLS
                applicationDefinition.addPortExposed(587, 10587); // Submission with startTLS
                applicationDefinition.addPortExposed(110, 10110); // POP3 with startTLS
                applicationDefinition.addPortExposed(143, 10143); // IMAP with startTLS
                applicationDefinition.addPortExposed(993, 10993); // IMAP with TLS
            }

            ActionsHandlerUtils.addOrUpdate(application, changes);

            // Link
            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.INSTALLED_ON, Machine.class, machines);
            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.RUN_AS, UnixUser.class, unixUsers);

        }

        CommonResourceLink.syncToLinks(services, changes, jamesEmailServer, LinkTypeConstants.MANAGES, Application.class, desiredManagedApplications);

    }

    private String toPem(WebsiteCertificate websiteCertificate) {
        StringBuilder pem = new StringBuilder();

        if (!Strings.isNullOrEmpty(websiteCertificate.getCertificate())) {
            pem.append(websiteCertificate.getCertificate()).append("\n");
        }
        if (!Strings.isNullOrEmpty(websiteCertificate.getPrivateKey())) {
            pem.append(websiteCertificate.getPrivateKey()).append("\n");
        }

        return pem.toString();
    }

}
