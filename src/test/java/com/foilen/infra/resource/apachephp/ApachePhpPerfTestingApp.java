/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import com.foilen.infra.plugin.core.system.memory.junits.ResourceServicesInMemoryTests;
import com.foilen.infra.plugin.core.system.memory.service.ResourceServicesInMemoryImpl;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.tools.TimeExecutionTools;

public class ApachePhpPerfTestingApp {

    public static void main(String[] args) {

        ResourceServicesInMemoryImpl resourceServicesInMemoryImpl = ResourceServicesInMemoryTests.init();

        // Create common
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser unixUser = new UnixUser(72000L, "myphp", "/home/myphp", null, null);
        Website website = new Website("myphp");
        website.getDomainNames().add("myphp.example.com");

        ChangesContext changes = new ChangesContext(resourceServicesInMemoryImpl);
        changes.resourceAdd(machine);
        changes.resourceAdd(unixUser);
        changes.resourceAdd(website);
        changes.linkAdd(website, LinkTypeConstants.INSTALLED_ON, machine);
        resourceServicesInMemoryImpl.changesExecute(changes);

        // Create some
        System.out.println("---[ Creation ]---");
        long timeInMs = TimeExecutionTools.measureInMs(() -> {
            for (int i = 0; i < 500; ++i) {

                ApachePhp apachePhp = new ApachePhp();
                apachePhp.setName("myphp_" + i);
                apachePhp.setBasePath("/home/myphp/php");
                apachePhp.setMainSiteRelativePath("/www/");
                apachePhp.setResourceEditorName(ApachePhpEditor.EDITOR_NAME);

                ApachePhpFolder apachePhpFolder = new ApachePhpFolder("/home/myphp/otherphp", "inside", "/other");
                changes.resourceAdd(apachePhp);
                changes.resourceAdd(apachePhpFolder);
                changes.linkAdd(apachePhp, LinkTypeConstants.RUN_AS, unixUser);
                changes.linkAdd(apachePhp, LinkTypeConstants.INSTALLED_ON, machine);
                changes.linkAdd(apachePhp, LinkTypeConstants.USES, apachePhpFolder);
                changes.linkAdd(website, LinkTypeConstants.POINTS_TO, apachePhp);
                if (i % 5 == 0) {
                    resourceServicesInMemoryImpl.changesExecute(changes);
                }
            }
            resourceServicesInMemoryImpl.changesExecute(changes);
        });

        System.out.println("Took " + timeInMs + " ms");

        System.out.println("---[ Update ]---");

    }

}
