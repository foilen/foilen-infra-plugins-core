/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.website;

import java.util.Arrays;
import java.util.TreeSet;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.DateTools;
import com.google.common.base.Joiner;

public class WebsiteTest extends AbstractCorePluginTest {

    private WebsiteCertificate createWebsiteCertificate(String certificate, String... domainNames) {
        WebsiteCertificate websiteCertificate = new WebsiteCertificate();
        websiteCertificate.setThumbprint(Joiner.on(",").join(domainNames));
        websiteCertificate.setDomainNames(new TreeSet<>(Arrays.asList(domainNames)));
        websiteCertificate.setStart(DateTools.parseDateOnly("2000-01-01"));
        websiteCertificate.setEnd(DateTools.parseDateOnly("2050-01-01"));
        websiteCertificate.setCertificate(certificate);
        return websiteCertificate;
    }

    @Test
    public void testWithAppHttp() {

        // Create resources
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser infraUrlUnixUser = new UnixUser(70000L, "infra_url_redirection", "/home/infra_url_redirection", null, null);
        UnixUser unixUser = new UnixUser(72000L, "myapp", "/home/myapp", null, null);
        Website website = new Website("myapp");
        website.getDomainNames().add("myapp.example.com");
        website.setApplicationEndpoint(DockerContainerEndpoints.HTTP_TCP);

        Application application = new Application();
        application.setName("myphp");

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(machine);
        changes.resourceAdd(infraUrlUnixUser);
        changes.resourceAdd(unixUser);
        changes.resourceAdd(application);
        changes.resourceAdd(website);

        // Create links
        changes.linkAdd(application, LinkTypeConstants.RUN_AS, unixUser);
        changes.linkAdd(application, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(website, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(website, LinkTypeConstants.POINTS_TO, application);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "WebsiteTest-testWithAppHttp-state.json", getClass(), true);

    }

    @Test
    public void testWithAppHttpHttpsAndOriginRewrite() {

        // Create resources
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser infraUrlUnixUser = new UnixUser(70000L, "infra_url_redirection", "/home/infra_url_redirection", null, null);
        UnixUser unixUser = new UnixUser(72000L, "myapp", "/home/myapp", null, null);
        Website websiteHttp = new Website("myapp-http");
        websiteHttp.getDomainNames().add("myapp.example.com");
        websiteHttp.setApplicationEndpoint(DockerContainerEndpoints.HTTP_TCP);
        WebsiteCertificate websiteHttpsCertificate = createWebsiteCertificate("CERTaaaCERT", "myapp.example.com");
        Website websiteHttps = new Website("myapp-https");
        websiteHttps.getDomainNames().add("myapp.example.com");
        websiteHttps.setApplicationEndpoint(DockerContainerEndpoints.HTTP_TCP);
        websiteHttps.setHttps(true);
        Website websiteHttps2 = new Website("myapp2-https");
        websiteHttps2.getDomainNames().add("myapp2.example.com");
        websiteHttps2.setApplicationEndpoint(DockerContainerEndpoints.HTTP_TCP);
        WebsiteCertificate websiteHttps2Certificate = createWebsiteCertificate("CERTbbbCERT", "myapp2.example.com");
        websiteHttps2.setHttps(true);
        websiteHttps2.setHttpsOriginToHttp(true);

        Application application = new Application();
        application.setName("myphp");

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(machine);
        changes.resourceAdd(infraUrlUnixUser);
        changes.resourceAdd(unixUser);
        changes.resourceAdd(application);
        changes.resourceAdd(websiteHttp);
        changes.resourceAdd(websiteHttps);
        changes.resourceAdd(websiteHttpsCertificate);
        changes.resourceAdd(websiteHttps2);
        changes.resourceAdd(websiteHttps2Certificate);

        // Create links
        changes.linkAdd(application, LinkTypeConstants.RUN_AS, unixUser);
        changes.linkAdd(application, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(websiteHttp, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(websiteHttp, LinkTypeConstants.POINTS_TO, application);
        changes.linkAdd(websiteHttps, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(websiteHttps, LinkTypeConstants.POINTS_TO, application);
        changes.linkAdd(websiteHttps, LinkTypeConstants.USES, websiteHttpsCertificate);
        changes.linkAdd(websiteHttps2, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(websiteHttps2, LinkTypeConstants.POINTS_TO, application);
        changes.linkAdd(websiteHttps2, LinkTypeConstants.USES, websiteHttps2Certificate);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "WebsiteTest-testWithAppHttpHttpsAndOriginRewrite-state-1.json", getClass(), true);

        // Update one cert
        changes.clear();
        websiteHttpsCertificate = getCommonServicesContext().getResourceService().resourceFindByPk(new WebsiteCertificate("myapp.example.com")).get();
        changes.resourceUpdate(websiteHttpsCertificate, createWebsiteCertificate("CERTcccCERT", "myapp.example.com"));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "WebsiteTest-testWithAppHttpHttpsAndOriginRewrite-state-2.json", getClass(), true);

    }

    @Test
    public void testWithAppHttps() {

        // Create resources
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser infraUrlUnixUser = new UnixUser(70000L, "infra_url_redirection", "/home/infra_url_redirection", null, null);
        UnixUser unixUser = new UnixUser(72000L, "myapp", "/home/myapp", null, null);
        Website website = new Website("myapp");
        website.getDomainNames().add("myapp.example.com");
        website.setApplicationEndpoint(DockerContainerEndpoints.HTTPS_TCP);

        Application application = new Application();
        application.setName("myphp");

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(machine);
        changes.resourceAdd(infraUrlUnixUser);
        changes.resourceAdd(unixUser);
        changes.resourceAdd(application);
        changes.resourceAdd(website);

        // Create links
        changes.linkAdd(application, LinkTypeConstants.RUN_AS, unixUser);
        changes.linkAdd(application, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(website, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(website, LinkTypeConstants.POINTS_TO, application);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "WebsiteTest-testWithAppHttps-state.json", getClass(), true);

    }

}
