/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.website;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonResourceLink;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionPortRedirect;
import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.infra.plugin.v1.model.haproxy.AbstractHaProxyConfigPortHttp;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfig;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfigEndpoint;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfigPortHttp;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfigPortHttpService;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfigPortHttps;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfigPortHttpsService;
import com.foilen.infra.plugin.v1.model.outputter.haproxy.HaProxyConfigOutput;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tuple.Tuple2;

public class WebsiteManageApplicationActionHandler extends AbstractBasics implements ActionHandler {

    private String machineName;

    public WebsiteManageApplicationActionHandler(String machineName) {
        this.machineName = machineName;
    }

    protected <T extends HaProxyConfigPortHttpService> void addWebsiteConfig(Machine machine, String machineName, IPApplicationDefinition applicationDefinition, AtomicInteger nextLocalPort,
            Website website, List<Tuple2<Application, List<Machine>>> pointsToApplicationOnMachines, AbstractHaProxyConfigPortHttp<T> configHttp) {

        // If is installed locally, just point to that one ; else, redirect to all other places
        List<String> installedLocallyApplicationNames = pointsToApplicationOnMachines.stream() //
                .filter(it -> it.getB().contains(machine)) //
                .map(it -> it.getA().getName()) //
                .sorted() //
                .distinct() //
                .collect(Collectors.toList());
        if (installedLocallyApplicationNames.isEmpty()) {
            logger.debug("[{}] {} is not installed locally. Will point to the remote endpoints", machineName, website);

            List<HaProxyConfigEndpoint> endpoints = new ArrayList<>();
            for (Tuple2<Application, List<Machine>> applicationMachines : pointsToApplicationOnMachines) {
                String remoteApplicationName = applicationMachines.getA().getName();
                for (Machine remoteMachine : applicationMachines.getB()) {
                    int localPort = nextLocalPort.getAndIncrement();
                    endpoints.add(new HaProxyConfigEndpoint("127.0.0.1", localPort).setSsl(DockerContainerEndpoints.HTTPS_TCP.equals(website.getApplicationEndpoint())));
                    applicationDefinition.addPortRedirect(localPort, remoteMachine.getName(), remoteApplicationName, website.getApplicationEndpoint());
                }
            }
            website.getDomainNames().forEach(hostname -> {
                T config = configHttp.createConfig();
                for (HaProxyConfigEndpoint endpoint : endpoints) {
                    config.getEndpoints().add(endpoint);
                }

                if (config instanceof HaProxyConfigPortHttpsService) {
                    ((HaProxyConfigPortHttpsService) config).setOriginToHttp(website.isHttpsOriginToHttp());
                }

                configHttp.getServiceByHostname().put(hostname, config);
            });
        } else {
            logger.debug("[{}] {} is installed locally. Will point locally only on applications {}", machineName, website, installedLocallyApplicationNames);

            List<HaProxyConfigEndpoint> endpoints = new ArrayList<>();
            for (String installedLocallyApplicationName : installedLocallyApplicationNames) {
                int localPort = nextLocalPort.getAndIncrement();
                endpoints.add(new HaProxyConfigEndpoint("127.0.0.1", localPort).setSsl(DockerContainerEndpoints.HTTPS_TCP.equals(website.getApplicationEndpoint())));
                applicationDefinition.addPortRedirect(localPort, IPApplicationDefinitionPortRedirect.LOCAL_MACHINE, installedLocallyApplicationName, website.getApplicationEndpoint());
            }
            configHttp.addService(website.getDomainNames(), endpoints.toArray(new HaProxyConfigEndpoint[endpoints.size()]));

            website.getDomainNames().forEach(hostname -> {
                T config = configHttp.getServiceByHostname().get(hostname);
                if (config instanceof HaProxyConfigPortHttpsService) {
                    ((HaProxyConfigPortHttpsService) config).setOriginToHttp(website.isHttpsOriginToHttp());
                }
            });

        }
    }

    private void deleteApplicationIfPresent(IPResourceService resourceService, ChangesContext changes, String applicationName) {
        Optional<Application> existingApplication = resourceService.resourceFindByPk(new Application(applicationName));
        if (existingApplication.isPresent()) {
            logger.info("Application {} exists. Deleting it", applicationName);
            changes.resourceDelete(existingApplication.get());
        }
    }

    @Override
    public void executeAction(CommonServicesContext services, ChangesContext changes) {

        logger.info("Processing machine {}", machineName);

        IPResourceService resourceService = services.getResourceService();
        Optional<Machine> o = resourceService.resourceFindByPk(new Machine(machineName));
        if (!o.isPresent()) {
            logger.info("{} is not present. Skipping", machineName);
            return;
        }
        Machine machine = o.get();
        String applicationName = "infra_web-" + machineName.replaceAll("\\.", "_");

        // Get the unix user
        String unixUserName = WebsiteChangesEventHandler.UNIX_USER_HA_PROXY_NAME;
        Optional<UnixUser> unixUserOptional = resourceService.resourceFind(resourceService.createResourceQuery(UnixUser.class).propertyEquals(UnixUser.PROPERTY_NAME, unixUserName));
        logger.info("Getting unix user {}. Is present: {}", unixUserName, unixUserOptional.isPresent());
        if (!unixUserOptional.isPresent()) {
            logger.info("Deleting any application since the unix user {} is not present yet", unixUserName);

            deleteApplicationIfPresent(resourceService, changes, applicationName);
            return;
        }
        UnixUser unixUser = unixUserOptional.get();

        // Get all the websites installed on this machine
        List<Website> websites = resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(Website.class, LinkTypeConstants.INSTALLED_ON, machine);
        websites.addAll(resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(Website.class, Website.LINK_TYPE_INSTALLED_ON_NO_DNS, machine));
        websites = websites.stream() //
                .sorted() //
                .distinct() //
                .collect(Collectors.toList());

        // HA Proxy
        Application haProxyApplication = new Application(applicationName);
        haProxyApplication.setDescription("Web HA Proxy for " + machineName);

        Optional<Application> existingHaProxyApplicationOptional = resourceService.resourceFindByPk(haProxyApplication);
        if (websites.isEmpty()) {
            logger.info("[{}] No websites to install", machineName);
            if (existingHaProxyApplicationOptional.isPresent()) {
                logger.info("[{}] Deleting existing HA Proxy", machineName);
                changes.resourceDelete(existingHaProxyApplicationOptional.get());
            }
        } else {
            logger.info("[{}] There are {} websites", machineName, websites.size());

            IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
            haProxyApplication.setApplicationDefinition(applicationDefinition);

            IPApplicationDefinitionAssetsBundle assetsBundle = applicationDefinition.addAssetsBundle();
            HaProxyConfig haProxyConfig = new HaProxyConfig();
            haProxyConfig.setTimeoutClientMs(10L * 60L * 1000L); // 10 minutes
            haProxyConfig.setTimeoutServerMs(10L * 60L * 1000L); // 10 minutes
            haProxyConfig.setTimeoutTunnelMs(10L * 60L * 1000L); // 10 minutes
            AtomicInteger nextLocalPort = new AtomicInteger(10000);
            for (Website website : websites) {
                logger.info("[{}] Processing {}", machineName, website);

                // Get the endpoints
                List<Application> pointsToApplications = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(website, LinkTypeConstants.POINTS_TO, Application.class);
                List<Tuple2<Application, List<Machine>>> pointsToApplicationOnMachines = pointsToApplications.stream() //
                        .map(app -> new Tuple2<>(app, resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(app, LinkTypeConstants.INSTALLED_ON, Machine.class))) //
                        .collect(Collectors.toList());
                if (pointsToApplications.isEmpty()) {
                    logger.debug("[{}] Cannot configure {} since it goes to no application", machineName, website);
                    continue;
                } else {
                    logger.debug("[{}] {} has {} applications that is points to", machineName, website, pointsToApplications.size());
                }
                boolean hasOneEndpoint = false;
                for (Tuple2<Application, List<Machine>> appMachine : pointsToApplicationOnMachines) {
                    hasOneEndpoint |= !appMachine.getB().isEmpty();
                }
                if (!hasOneEndpoint) {
                    logger.debug("[{}] {} has applications that is points to, but none is installed on a machine", machineName, website);
                    continue;
                }

                // Create configuration for http or https
                if (website.isHttps()) {

                    // HTTPS
                    List<WebsiteCertificate> websiteCertificates = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(website, LinkTypeConstants.USES, WebsiteCertificate.class);
                    if (websiteCertificates.size() != 1) {
                        logger.debug("[{}] Cannot configure {} since we are expecting 1 website certificate and got {}", machineName, website, websiteCertificates.size());
                        continue;
                    } else {
                        WebsiteCertificate websiteCertificate = websiteCertificates.get(0);
                        HaProxyConfigPortHttps configHttps = haProxyConfig.addPortHttps(4433, "/certs");

                        // Add certificate
                        StringBuilder certContent = new StringBuilder();
                        if (websiteCertificate.getCertificate() != null) {
                            certContent.append(websiteCertificate.getCertificate());
                            certContent.append("\n");
                        }
                        if (websiteCertificate.getCaCertificate() != null) {
                            certContent.append(websiteCertificate.getCaCertificate());
                            certContent.append("\n");
                        }
                        if (websiteCertificate.getPrivateKey() != null) {
                            certContent.append(websiteCertificate.getPrivateKey());
                            certContent.append("\n");
                        }
                        for (String domainName : website.getDomainNames()) {
                            logger.debug("[{}] Installing certificate for {}", machineName, domainName);
                            assetsBundle.addAssetContent("/certs/" + domainName + ".pem", certContent.toString());
                        }

                        addWebsiteConfig(machine, machineName, applicationDefinition, nextLocalPort, website, pointsToApplicationOnMachines, configHttps);
                    }
                } else {

                    // HTTP
                    HaProxyConfigPortHttp configHttp = haProxyConfig.addPortHttp(8080);
                    addWebsiteConfig(machine, machineName, applicationDefinition, nextLocalPort, website, pointsToApplicationOnMachines, configHttp);

                }
            }

            applicationDefinition.setFrom("foilen/fcloud-docker-haproxy:1.6.3-002");

            applicationDefinition.addPortExposed(80, 8080);
            applicationDefinition.addPortExposed(443, 4433);

            assetsBundle.addAssetContent("/haproxy.cfg", HaProxyConfigOutput.toConfigFile(haProxyConfig));

            applicationDefinition.addService("haproxy", HaProxyConfigOutput.toRun(haProxyConfig, "/haproxy.cfg"));

            applicationDefinition.setRunAs(unixUser.getId());
            applicationDefinition.addContainerUserToChangeId("haproxy", unixUser.getId());

            // Create or update
            if (existingHaProxyApplicationOptional.isPresent()) {
                logger.info("Update existing application");
                changes.resourceUpdate(existingHaProxyApplicationOptional.get(), haProxyApplication);
            } else {
                logger.info("Create new application");
                changes.resourceAdd(haProxyApplication);
            }

            // Link machine -> MANAGE-> application
            changes.linkAdd(machine, LinkTypeConstants.MANAGES, haProxyApplication);

            CommonResourceLink.syncToLinks(services, changes, haProxyApplication, LinkTypeConstants.INSTALLED_ON, Machine.class, Collections.singletonList(machine));
            CommonResourceLink.syncToLinks(services, changes, haProxyApplication, LinkTypeConstants.RUN_AS, UnixUser.class, Collections.singletonList(unixUser));

        }

    }

}
