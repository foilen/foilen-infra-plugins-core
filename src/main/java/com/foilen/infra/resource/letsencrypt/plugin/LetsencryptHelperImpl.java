/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.plugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.TimerEventContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.dns.DnsEntry;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.infra.resource.letsencrypt.acme.AcmeService;
import com.foilen.infra.resource.letsencrypt.acme.AcmeServiceImpl;
import com.foilen.infra.resource.letsencrypt.acme.LetsencryptException;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Joiner;

public class LetsencryptHelperImpl extends AbstractBasics implements LetsencryptHelper {

    static SSLContext allTrustingSslContext;

    static {

        TrustManager[] trustManager = new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        } };

        try {
            allTrustingSslContext = SSLContext.getInstance("SSL");
            allTrustingSslContext.init(null, trustManager, new java.security.SecureRandom());
        } catch (Exception e) {
            throw new LetsencryptException("Could not create an SSL that trusts all certs", e);
        }
    }

    private static Function<LetsencryptConfig, AcmeService> _acmeServiceGenerator = (config) -> new AcmeServiceImpl(config);

    public static Function<LetsencryptConfig, AcmeService> getAcmeServiceGenerator() {
        return _acmeServiceGenerator;
    }

    public static void setAcmeServiceGenerator(Function<LetsencryptConfig, AcmeService> acmeServiceGenerator) {
        _acmeServiceGenerator = acmeServiceGenerator;
    }

    @Override
    public void checkUrlOrFail(String url) {

        try {
            logger.info("Checking for url {}", url);
            HttpClient client = HttpClient.newBuilder() //
                    .version(Version.HTTP_1_1) //
                    .followRedirects(Redirect.NORMAL) //
                    .connectTimeout(Duration.ofSeconds(20)) //
                    .sslContext(allTrustingSslContext) //
                    .build();
            HttpResponse<String> response = client.send(HttpRequest.newBuilder() //
                    .GET().uri(new URI(url)) //
                    .build(), BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new LetsencryptException("Could not get the url. " + response.statusCode() + " " + response.body());
            }
        } catch (Exception e) {
            throw new LetsencryptException("Could not get the url", e);
        }

    }

    /**
     * Get the ACME configuration, create the challenges and start the timer to complete.
     *
     * @param services
     *            all services
     * @param changes
     *            the changes to make
     * @param certificatesToUpdate
     *            the certificates to generate challenges for
     */
    @Override
    public void createChallengesAndCreateTimer(CommonServicesContext services, ChangesContext changes, List<WebsiteCertificate> certificatesToUpdate) {

        IPResourceService resourceService = services.getResourceService();

        // Get the config
        logger.info("Getting the config");
        Optional<LetsencryptConfig> configOptional = resourceService.resourceFind(resourceService.createResourceQuery(LetsencryptConfig.class));
        LetsencryptConfig config;
        logger.info("Config is present? {}", configOptional.isPresent());
        if (configOptional.isPresent()) {
            config = configOptional.get();
        } else {
            throw new IllegalUpdateException("Could not find a LetsencryptConfig. Create one first");
        }

        String tagName = config.getTagName();
        if (tagName == null) {
            throw new IllegalUpdateException("The LetsencryptConfig does not have a tag name");
        }

        // Remove certs that recently failed
        certificatesToUpdate.removeIf(websiteCertificate -> recentlyFailed(websiteCertificate));

        if (certificatesToUpdate.isEmpty()) {
            logger.info("No certs to update");
            return;
        }

        logger.info("Will update certificates: {}", certificatesToUpdate.stream().flatMap(it -> it.getDomainNames().stream()).sorted().collect(Collectors.toList()));
        AcmeService acmeService = _acmeServiceGenerator.apply(config);

        // Get the challenges
        logger.info("Getting the challenges");
        List<String> domainsWithoutChallenge = new ArrayList<>();
        Map<String, Tuple2<Order, Dns01Challenge>> dnsChallengeByDomain = new HashMap<>();
        for (WebsiteCertificate certificate : certificatesToUpdate) {

            // Check if LetsencryptWithFileAttachable -> USES -> WebsiteCertificate
            List<LetsEncryptWithFileAttachable> letsencryptWithFileAttachables = resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(LetsEncryptWithFileAttachable.class,
                    LinkTypeConstants.USES, certificate);
            logger.info("Certificate {} has {} files attachables", certificate.getResourceName(), letsencryptWithFileAttachables.size());

            if (letsencryptWithFileAttachables.isEmpty()) {
                logger.info("Certificate {} uses DNS validation", certificate.getResourceName());
                String domain = certificate.getDomainNames().stream().findFirst().get();
                Tuple2<Order, Dns01Challenge> orderAndDnsChallenge;
                try {
                    orderAndDnsChallenge = acmeService.challengeDnsInit(domain);
                    Dns01Challenge dnsChallenge = orderAndDnsChallenge.getB();
                    dnsChallengeByDomain.put(domain, orderAndDnsChallenge);
                    String digest = dnsChallenge.getDigest();

                    // Add DnsEntries if does not already exist
                    DnsEntry dnsEntry = new DnsEntry("_acme-challenge." + domain, DnsEntryType.TXT, digest);
                    Optional<DnsEntry> existingDnsEntry = resourceService.resourceFindByPk(dnsEntry);
                    if (!existingDnsEntry.isPresent()) {
                        changes.resourceAdd(dnsEntry);
                        changes.linkAdd(certificate, LinkTypeConstants.MANAGES, dnsEntry);
                        changes.tagAdd(dnsEntry, tagName);
                    }
                } catch (LetsencryptException e) {
                    logger.error("Cannot get the challenge for domain {}", domain, e);
                    domainsWithoutChallenge.add(domain + " : " + getAllMessages(e));
                } catch (Exception e) {
                    logger.error("Unexpected failure while getting the challenge for domain {}", domain, e);
                    domainsWithoutChallenge.add(domain + " : " + getAllMessages(e));
                }
            } else {
                logger.info("Certificate {} uses http validation", certificate.getResourceName());

                // Skip if already started
                if (letsencryptWithFileAttachables.stream().anyMatch(it -> recentlyStarted(it))) {
                    logger.info("Recently started. Skipping", certificate.getResourceName());
                    continue;
                }

                String domain = certificate.getDomainNames().stream().findFirst().get();
                Tuple2<Order, Http01Challenge> orderAndHttpChallenge;
                try {
                    orderAndHttpChallenge = acmeService.challengeHttpInit(domain);
                    Http01Challenge httpChallenge = orderAndHttpChallenge.getB();
                    String fileName = httpChallenge.getToken();
                    String fileContent = httpChallenge.getAuthorization();

                    // Update attachable
                    letsencryptWithFileAttachables.forEach(a -> {
                        a.getMeta().put(LetsEncryptWithFileAttachable.META_FILE_NAME, fileName);
                        a.getMeta().put(LetsEncryptWithFileAttachable.META_FILE_CONTENT, fileContent);
                        a.getMeta().put(LetsEncryptWithFileAttachable.META_LAST_START, String.valueOf(System.currentTimeMillis()));
                        changes.resourceUpdate(a);
                    });

                    // Start the timer
                    String url = "http://" + domain + "/.well-known/acme-challenge/" + fileName;
                    logger.info("Start the Waiting for the HTTP: {} ; url: {} ; content: {}", domain, url, fileContent);
                    services.getTimerService()
                            .timerAdd(new TimerEventContext(
                                    new LetsEncryptRefreshOldCertsWaitHttpTimer(acmeService, this, domain, orderAndHttpChallenge.getA(), orderAndHttpChallenge.getB(), url, certificate), //
                                    "Let Encrypt - Complete - Wait URL", //
                                    Calendar.SECOND, //
                                    10, //
                                    true, //
                                    false));

                } catch (LetsencryptException e) {
                    logger.error("Cannot get the challenge for domain {}", domain, e);
                    domainsWithoutChallenge.add(domain + " : " + getAllMessages(e));
                } catch (Exception e) {
                    logger.error("Unexpected failure while getting the challenge for domain {}", domain, e);
                    domainsWithoutChallenge.add(domain + " : " + getAllMessages(e));
                }
            }
        }

        if (!domainsWithoutChallenge.isEmpty()) {
            services.getMessagingService().alertingWarn("Let's Encrypt - Domains Without Challenge", Joiner.on('\n').join(domainsWithoutChallenge));
        }

        if (!dnsChallengeByDomain.isEmpty()) {
            // Add the waiting domain
            String dnsWaitDomain = "z" + SecureRandomTools.randomHexString(5).toLowerCase() + config.getDnsUpdatedSubDomain();
            logger.info("Adding the DNS Wait domain {}", dnsWaitDomain);

            DnsEntry dnsEntry = new DnsEntry(dnsWaitDomain, DnsEntryType.A, "127.0.0.1");
            changes.resourceAdd(dnsEntry);
            changes.linkAdd(config, LinkTypeConstants.MANAGES, dnsEntry);
            changes.tagAdd(dnsEntry, tagName);

            // Start a new timer for the DNS
            logger.info("Start the Waiting for the DNS");
            services.getTimerService().timerAdd(new TimerEventContext(new LetsEncryptRefreshOldCertsWaitDnsTimer(acmeService, this, dnsWaitDomain, dnsChallengeByDomain), //
                    "Let Encrypt - Complete - Wait DNS", //
                    Calendar.MINUTE, //
                    2, //
                    true, //
                    false));

        }

        logger.info("Done creating the challenges");

    }

    @Override
    public String getAllMessages(Throwable e) {

        StringBuilder messages = new StringBuilder();

        boolean first = true;
        while (e != null) {
            if (first) {
                first = true;
            } else {
                messages.append(" ; ");
            }
            if (e.getMessage() != null) {
                messages.append(e.getMessage());
            }

            e = e.getCause();
        }

        return messages.toString();
    }

    @Override
    public boolean recentlyFailed(WebsiteCertificate websiteCertificate) {
        long beforeTime = System.currentTimeMillis() - 6 * 60 * 60000; // 6 hours for 4 times a day
        String value = websiteCertificate.getMeta().get(LetsencryptHelper.LAST_FAILURE);
        if (value != null) {
            try {
                long lastFailure = Long.valueOf(value);
                boolean recentlyFailed = lastFailure > beforeTime;
                if (recentlyFailed) {
                    logger.info("{} recently failed. On {}", websiteCertificate.getDomainNames(), DateTools.formatFull(new Date(lastFailure)));
                }
                return recentlyFailed;
            } catch (Exception e) {
            }
        }
        return false;
    }

    @Override
    public boolean recentlyStarted(LetsEncryptWithFileAttachable letsEncryptWithFileAttachable) {
        long beforeTime = System.currentTimeMillis() - 10 * 60000;
        String value = letsEncryptWithFileAttachable.getMeta().get(LetsEncryptWithFileAttachable.META_LAST_START);
        if (value != null) {
            try {
                long lastStart = Long.valueOf(value);
                boolean recentlyStarted = lastStart > beforeTime;
                if (recentlyStarted) {
                    logger.info("{} recently started. On {}", letsEncryptWithFileAttachable.getName(), DateTools.formatFull(new Date(lastStart)));
                }
                return recentlyStarted;
            } catch (Exception e) {
            }
        }
        return false;
    }

}
