/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.plugin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.util.CSRBuilder;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.TimerEventContext;
import com.foilen.infra.plugin.v1.core.eventhandler.TimerEventHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.letsencrypt.acme.AcmeService;
import com.foilen.infra.resource.letsencrypt.acme.LetsencryptException;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.infra.resource.webcertificate.helper.CertificateHelper;
import com.foilen.smalltools.crypt.spongycastle.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.spongycastle.asymmetric.RSACrypt;
import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;
import com.foilen.smalltools.crypt.spongycastle.cert.RSATools;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Joiner;

public class LetsEncryptRefreshOldCertsWaitHttpTimer extends AbstractLetsEncryptRefreshOldCertsWaitTimer implements TimerEventHandler {

    private String domain;

    private Tuple2<Order, Http01Challenge> challenge;

    private String url;

    private WebsiteCertificate websiteCertificate;

    private Date expiration;

    public LetsEncryptRefreshOldCertsWaitHttpTimer(AcmeService acmeService, LetsencryptHelper letsencryptHelper, String domain, Tuple2<Order, Http01Challenge> challenge, String url,
            WebsiteCertificate websiteCertificate) {
        super(acmeService, letsencryptHelper);
        this.domain = domain;
        this.challenge = challenge;
        this.url = url;
        this.websiteCertificate = websiteCertificate;
        this.expiration = DateTools.addDate(Calendar.MINUTE, 10);
    }

    @Override
    public void timerHandler(CommonServicesContext services, ChangesContext changes, TimerEventContext event) {

        try {

            // Wait for the url
            try {
                // Check without cert validation
                letsencryptHelper.checkUrlOrFail(url);
            } catch (Exception e) {

                // Check if expired
                if (DateTools.isAfter(new Date(), expiration)) {
                    logger.info("Url {} not present. No more waiting because it expired", url, e);
                    return;
                }

                // Wait 10 seconds
                logger.info("Url {} not present. Waiting 10 seconds", url, e);
                services.getTimerService().timerAdd(new TimerEventContext(this, //
                        "Let Encrypt - Complete - Wait URL", //
                        Calendar.SECOND, //
                        10, //
                        true, //
                        false));
                return;
            }

            logger.info("Url {} found", url);

            // Complete the challenges
            logger.info("Complete challenges");
            IPResourceService resourceService = services.getResourceService();
            List<String> failures = new ArrayList<>();
            try {
                logger.info("Complete the challenge for certificate: {}", domain);
                acmeService.challengeComplete(challenge.getB());
            } catch (LetsencryptException e) {
                // Challenge failed
                logger.info("Failed the challenge for certificate: {}", domain);
                failures.add(domain + " : " + getAllMessages(e));

                // Update meta as failure
                resourceService.resourceFindAll( //
                        resourceService.createResourceQuery(WebsiteCertificate.class) //
                                .addEditorEquals(LetsEncryptWebsiteCertificateEditor.EDITOR_NAME) //
                                .propertyEquals(WebsiteCertificate.PROPERTY_DOMAIN_NAMES, Collections.singleton(domain))) //
                        .forEach(websiteCertificate -> {
                            websiteCertificate.getMeta().put(LetsencryptHelper.LAST_FAILURE, String.valueOf(System.currentTimeMillis()));
                            changes.resourceUpdate(websiteCertificate);
                        });

            }

            List<String> successes = new ArrayList<>();
            List<Tuple2<AsymmetricKeys, RSACertificate>> keysAndCerts = new ArrayList<>();
            if (failures.isEmpty()) {
                // Get the certificates for the successful ones
                logger.info("Get the certificate from Lets Encrypt");
                AsymmetricKeys asymmetricKeys = RSACrypt.RSA_CRYPT.generateKeyPair(4096);

                CSRBuilder csrb = new CSRBuilder();
                csrb.addDomain(domain);

                try {
                    logger.info("Getting certificate for: {}", domain);
                    csrb.sign(RSATools.createKeyPair(asymmetricKeys));
                    byte[] csr = csrb.getEncoded();
                    RSACertificate certificate = acmeService.requestCertificate(challenge.getA(), csr);
                    certificate.setKeysForSigning(asymmetricKeys);
                    keysAndCerts.add(new Tuple2<>(asymmetricKeys, certificate));

                    logger.info("Successfully updated certificate: {}", domain);
                    successes.add(domain);
                } catch (Exception e) {
                    // Cert creation failed
                    logger.info("Failed to retrieve the certificate for: {}", domain);
                    failures.add(domain + " : " + getAllMessages(e));
                }
            }

            if (!failures.isEmpty()) {
                services.getMessagingService().alertingWarn("Let's Encrypt - Domains Couldn't get certificate", Joiner.on('\n').join(failures));
            }
            if (!successes.isEmpty()) {
                services.getMessagingService().alertingInfo("Let's Encrypt - Domains that got a new certificate", Joiner.on('\n').join(successes));
            }

            // Update the certificates
            logger.info("Update the certificates in the system");
            for (Tuple2<AsymmetricKeys, RSACertificate> entry : keysAndCerts) {
                RSACertificate rsaCertificate = entry.getB();
                WebsiteCertificate newCert = CertificateHelper.toWebsiteCertificate(CA_CERTIFICATE_TEXT, rsaCertificate);
                newCert.setResourceEditorName(LetsEncryptWebsiteCertificateEditor.EDITOR_NAME);

                changes.resourceUpdate(websiteCertificate, newCert);
            }

        } catch (Exception e) {
            logger.error("Problem while managing Lets Encrypt", e);
            services.getMessagingService().alertingError("Problem while managing Lets Encrypt", e.getMessage());
        } finally {
            logger.info("Timer completed");
        }

    }

}
