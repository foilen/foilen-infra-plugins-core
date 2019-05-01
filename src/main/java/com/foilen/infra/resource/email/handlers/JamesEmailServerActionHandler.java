/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.handlers;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.exception.ProblemException;
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
import com.foilen.infra.resource.webcertificate.helper.CertificateHelper;
import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;
import com.foilen.smalltools.crypt.spongycastle.cert.RSATools;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.FreemarkerTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Strings;

public class JamesEmailServerActionHandler extends AbstractBasics implements ActionHandler {

    private String serverName;

    public JamesEmailServerActionHandler(String serverName) {
        this.serverName = serverName;
    }

    protected byte[] createKeystore(ChangesContext changes, JamesEmailServer jamesEmailServer, String certType, List<WebsiteCertificate> certs) {

        WebsiteCertificate cert = certs.get(0);

        if (StringTools.safeEquals(jamesEmailServer.getMeta().get("certThumbprint-" + certType), cert.getThumbprint())) {
            // Return same
            logger.debug("Keystore for cert {} is already generated", certType);
            String certBase64 = jamesEmailServer.getMeta().get("certKeystore-" + certType);
            if (Strings.isNullOrEmpty(certBase64)) {
                logger.warn("Keystore for cert {} was already generated, but it is not present. Will regenerate", certType);
            } else {
                return Base64.getDecoder().decode(certBase64);
            }
        }

        logger.debug("Keystore for cert {} is not generated. Will generate", certType);
        jamesEmailServer.getMeta().put("certThumbprint-" + certType, cert.getThumbprint());

        try {
            RSACertificate rsaCert = CertificateHelper.toRSACertificate(cert);

            char[] password = new char[] { 'j', 'a', 'm', 'e', 's' };
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, password);

            String alias = "james";
            Certificate certificate = rsaCert.getCertificate();
            keyStore.setCertificateEntry(alias, certificate);
            Key key = RSATools.createPrivateKey(rsaCert.getKeysForSigning());
            keyStore.setKeyEntry(alias, key, password, new Certificate[] { certificate });

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            keyStore.store(outStream, password);
            byte[] byteArray = outStream.toByteArray();
            jamesEmailServer.getMeta().put("certKeystore-" + certType, Base64.getEncoder().encodeToString(byteArray));
            changes.resourceUpdate(jamesEmailServer);
            return byteArray;
        } catch (Exception e) {
            throw new ProblemException("Could not create the keystore", e);
        }

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
            Long unixUserId = unixUser.getId();
            MariaDBDatabase mariadbDatabase = mariadbDatabases.get(0);
            MariaDBUser mariadbUser = mariadbUsers.get(0);

            // Create an Application
            Application application = ActionsHandlerUtils.getOrCreateAnApplication(resourceService, jamesEmailServer.getName());
            desiredManagedApplications.add(application);
            application.setDescription(jamesEmailServer.getResourceDescription());

            IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
            application.setApplicationDefinition(applicationDefinition);
            applicationDefinition.setRunAs(unixUserId);

            applicationDefinition.setFrom("foilen/fcloud-docker-email:" + jamesEmailServer.getVersion());

            // Start script
            IPApplicationDefinitionAssetsBundle assetsBundle = applicationDefinition.addAssetsBundle();
            assetsBundle.addAssetResource("/james-start.sh", "/com/foilen/infra/resource/email/james/james-start.sh");

            // Config files
            Map<String, Object> model = new HashMap<>();
            model.put("postmasterEmail", jamesEmailServer.getPostmasterEmail());
            model.put("disableBounceNotifyPostmaster", jamesEmailServer.isDisableBounceNotifyPostmaster());
            model.put("disableBounceNotifySender", jamesEmailServer.isDisableBounceNotifySender());
            model.put("disableRelayDeniedNotifyPostmaster", jamesEmailServer.isDisableRelayDeniedNotifyPostmaster());
            model.put("enableDebugDumpMessagesDetails", jamesEmailServer.isEnableDebugDumpMessagesDetails());
            model.put("dbName", mariadbDatabase.getName());
            model.put("dbUser", mariadbUser.getName());
            model.put("dbPass", mariadbUser.getPassword());
            assetsBundle.addAssetResource("/james-server-app/conf/domainlist.xml", "/com/foilen/infra/resource/email/james/domainlist.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/dnsservice.xml", "/com/foilen/infra/resource/email/james/dnsservice.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/events.xml", "/com/foilen/infra/resource/email/james/events.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/imapserver.xml", "/com/foilen/infra/resource/email/james/imapserver.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/indexer.xml", "/com/foilen/infra/resource/email/james/indexer.xml");
            assetsBundle.addAssetContent("/james-server-app/conf/james-database.properties",
                    FreemarkerTools.processTemplate("/com/foilen/infra/resource/email/james/james-database.properties.ftl", model));
            assetsBundle.addAssetResource("/james-server-app/conf/jmx.properties", "/com/foilen/infra/resource/email/james/jmx.properties");
            assetsBundle.addAssetResource("/james-server-app/conf/lmtpserver.xml", "/com/foilen/infra/resource/email/james/lmtpserver.xml");
            if (jamesEmailServer.isEnableDebuglogs()) {
                assetsBundle.addAssetResource("/james-server-app/conf/log4j.properties", "/com/foilen/infra/resource/email/james/log4j-debug.properties");
            } else {
                assetsBundle.addAssetResource("/james-server-app/conf/log4j.properties", "/com/foilen/infra/resource/email/james/log4j.properties");
            }
            assetsBundle.addAssetResource("/james-server-app/conf/mailbox.xml", "/com/foilen/infra/resource/email/james/mailbox.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/mailrepositorystore.xml", "/com/foilen/infra/resource/email/james/mailrepositorystore.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/managesieveserver.xml", "/com/foilen/infra/resource/email/james/managesieveserver.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/pop3server.xml", "/com/foilen/infra/resource/email/james/pop3server.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/quota.xml", "/com/foilen/infra/resource/email/james/quota.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/recipientrewritetable.xml", "/com/foilen/infra/resource/email/james/recipientrewritetable.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/sieverepository.xml", "/com/foilen/infra/resource/email/james/sieverepository.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/smtpserver.xml", "/com/foilen/infra/resource/email/james/smtpserver.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/usersrepository.xml", "/com/foilen/infra/resource/email/james/usersrepository.xml");
            assetsBundle.addAssetResource("/james-server-app/conf/wrapper.conf", "/com/foilen/infra/resource/email/james/wrapper.conf");

            // Keystores
            assetsBundle.addAssetContent("/james-server-app/conf/keystore-imaps", createKeystore(changes, jamesEmailServer, "imaps", certsImap));
            assetsBundle.addAssetContent("/james-server-app/conf/keystore-pop3s", createKeystore(changes, jamesEmailServer, "pop3s", certsPop3));
            assetsBundle.addAssetContent("/james-server-app/conf/keystore-smtps", createKeystore(changes, jamesEmailServer, "smtp", certsSmtp));

            // Config for Manager Daemon Service
            EmailManagerConfig emailManagerConfig = new EmailManagerConfig();
            EmailManagerConfigDatabase emailManagerConfigDatabase = new EmailManagerConfigDatabase();
            emailManagerConfigDatabase.setHostname("127.0.0.1");
            emailManagerConfigDatabase.setDatabase(mariadbDatabase.getName());
            emailManagerConfigDatabase.setUsername(mariadbUser.getName());
            emailManagerConfigDatabase.setPassword(mariadbUser.getPassword());
            emailManagerConfig.setDatabase(emailManagerConfigDatabase);
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
            model.put("domainAndRelais", domainAndRelais);
            assetsBundle.addAssetContent("/james-server-app/conf/mailetcontainer.xml", FreemarkerTools.processTemplate("/com/foilen/infra/resource/email/james/mailetcontainer.xml.ftl", model));

            Collections.sort(emailManagerConfig.getDomains());
            Collections.sort(emailManagerConfig.getAccounts());
            Collections.sort(emailManagerConfig.getRedirections());
            applicationDefinition.addCopyWhenStartedContent("/emailManagerConfig.json", JsonTools.prettyPrint(emailManagerConfig));

            // Attach parts in a deterministic order
            logger.debug("attachedParts ; amount {}", attachedParts.size());
            attachedParts.stream() //
                    .sorted((a, b) -> a.getResourceName().compareTo(b.getResourceName())) //
                    .forEach(attachedPart -> {
                        logger.debug("Attaching {} with type {}", attachedPart.getResourceName(), attachedPart.getClass().getName());
                        attachedPart.attachTo(new AttachablePartContext().setServices(services).setApplication(application).setApplicationDefinition(applicationDefinition));
                    });

            // Logs
            applicationDefinition.addVolume(new IPApplicationDefinitionVolume( //
                    unixUser.getHomeFolder() + "/_" + jamesEmailServer.getName() + "/log", //
                    "/james-server-app/log"));

            // User
            applicationDefinition.addContainerUserToChangeId("james", unixUserId);
            applicationDefinition.addBuildStepCommand("chmod 755 /james-start.sh && chown james:james -R /james-server-app/");

            // Service
            applicationDefinition.addService("james", "/james-start.sh");
            applicationDefinition.addService("james-manager", "/opt/james-manager/bin/james-manager --configFile /emailManagerConfig.json");

            applicationDefinition.addPortEndpoint(10025, DockerContainerEndpoints.SMTP_TCP);
            applicationDefinition.addPortExposed(25, 10025); // SMTP with startTLS
            applicationDefinition.addPortExposed(465, 10465); // SMTP with TLS
            applicationDefinition.addPortExposed(587, 10587); // Submission with startTLS
            applicationDefinition.addPortExposed(110, 10110); // POP3 with startTLS
            applicationDefinition.addPortExposed(143, 10143); // IMAP with startTLS
            applicationDefinition.addPortExposed(993, 10993); // IMAP with TLS

            ActionsHandlerUtils.addOrUpdate(application, changes);

            // Link
            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.INSTALLED_ON, Machine.class, machines);
            CommonResourceLink.syncToLinks(services, changes, application, LinkTypeConstants.RUN_AS, UnixUser.class, unixUsers);

        }

        CommonResourceLink.syncToLinks(services, changes, jamesEmailServer, LinkTypeConstants.MANAGES, Application.class, desiredManagedApplications);

    }

}
