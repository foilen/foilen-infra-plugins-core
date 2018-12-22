/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.website;

import java.util.Collections;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.unixuser.UnixUser;

public class WebsiteTest extends AbstractCorePluginTest {

    @Test
    public void testWithAppHttp() {

        // Create resources
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser infraUrlUnixUser = new UnixUser(70000L, "infra_url_redirection", "/home/infra_url_redirection", null, null);
        UnixUser unixUser = new UnixUser(72000L, "myapp", "/home/myapp", null, null);
        Website website = new Website("myapp");
        website.setDomainNames(Collections.singleton("myapp.example.com"));
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
    public void testWithAppHttps() {

        // Create resources
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser infraUrlUnixUser = new UnixUser(70000L, "infra_url_redirection", "/home/infra_url_redirection", null, null);
        UnixUser unixUser = new UnixUser(72000L, "myapp", "/home/myapp", null, null);
        Website website = new Website("myapp");
        website.setDomainNames(Collections.singleton("myapp.example.com"));
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
