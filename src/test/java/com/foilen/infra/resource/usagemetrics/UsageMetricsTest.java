/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.usagemetrics;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.core.system.junits.ResourcesDump;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.mongodb.MongoDBServer;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.usagemetrics.editors.UsageMetricsConfigEditor;
import com.foilen.infra.resource.usagemetrics.resources.UsageMetricsConfig;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.JsonTools;

public class UsageMetricsTest extends AbstractCorePluginTest {

    @Test
    public void test_update() {

        // Create some machines and the mongodb
        InternalChangeService internalChangeService = getInternalServicesContext().getInternalChangeService();
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        internalChangeService.changesExecute(new ChangesContext(resourceService) //
                .resourceAdd(new Machine("f1.example.com", "127.0.0.1")) //
                .resourceAdd(new Machine("f2.example.com", "127.0.0.2")) //

                .resourceAdd(new UnixUser(80000L, "mongo", null, null)) //
                .resourceAdd(new MongoDBServer("mongoUsage", "Database for Usage Metrics", "4.0.12", "qwerty")) //
                .linkAdd(new MongoDBServer("mongoUsage"), LinkTypeConstants.RUN_AS, new UnixUser(80000L)) //
                .linkAdd(new MongoDBServer("mongoUsage"), LinkTypeConstants.INSTALLED_ON, new Machine("f1.example.com")) //

        );
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update-1.json", getClass(), true);

        Machine machineF1 = resourceService.resourceFindByPk(new Machine("f1.example.com")).get();
        MongoDBServer mongoDbServer = resourceService.resourceFindByPk(new MongoDBServer("mongoUsage")).get();

        // Create the config
        HashMap<String, String> form = new HashMap<>();
        form.put(UsageMetricsConfig.PROPERTY_VERSION, "1.0.0");
        form.put(UsageMetricsConfig.PROPERTY_HOST_KEY_SALT, "aabbcc");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_DATABASE, "mdb");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_USER, "muser");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_PASSWORD, "mpass");
        form.put("mongoDbServer", String.valueOf(mongoDbServer.getInternalId()));
        form.put("centralInstalledOn", String.valueOf(machineF1.getInternalId()));

        assertEditorNoErrors(null, new UsageMetricsConfigEditor(), form);

        // Update the UID
        resourceService.resourceFindAll(resourceService.createResourceQuery(UsageMetricsConfig.class)).forEach(umc -> {
            umc.setUid("uid123uid");
            internalChangeService.changesExecute(new ChangesContext(resourceService) //
                    .resourceUpdate(umc) //
            );
        });
        String configId = resourceService.resourceFindByPk(new UsageMetricsConfig("uid123uid")).get().getInternalId();

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update-2.json", getClass(), true);

        // Change the version
        form = new HashMap<>();
        form.put(UsageMetricsConfig.PROPERTY_VERSION, "2.0.0");
        form.put(UsageMetricsConfig.PROPERTY_HOST_KEY_SALT, "aabbcc");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_DATABASE, "mdb");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_USER, "muser");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_PASSWORD, "mpass");
        form.put("mongoDbServer", String.valueOf(mongoDbServer.getInternalId()));
        form.put("centralInstalledOn", String.valueOf(machineF1.getInternalId()));

        assertEditorNoErrors(configId, new UsageMetricsConfigEditor(), form);

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update-3.json", getClass(), true);

        // Change the salt
        form = new HashMap<>();
        form.put(UsageMetricsConfig.PROPERTY_VERSION, "2.0.0");
        form.put(UsageMetricsConfig.PROPERTY_HOST_KEY_SALT, "ddeeff");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_DATABASE, "mdb");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_USER, "muser");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_PASSWORD, "mpass");
        form.put("mongoDbServer", String.valueOf(mongoDbServer.getInternalId()));
        form.put("centralInstalledOn", String.valueOf(machineF1.getInternalId()));

        assertEditorNoErrors(configId, new UsageMetricsConfigEditor(), form);

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update-4.json", getClass(), true);

        // Add a machine
        internalChangeService.changesExecute(new ChangesContext(resourceService) //
                .resourceAdd(new Machine("f3.example.com", "127.0.0.3")) //
        );

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update-5.json", getClass(), true);

        // Remove a machine
        internalChangeService.changesExecute(new ChangesContext(resourceService) //
                .resourceDelete(new Machine("f3.example.com")) //
        );

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update-4.json", getClass(), true);

        // Remove the config (cleanup)
        internalChangeService.changesExecute(new ChangesContext(resourceService) //
                .resourceDelete(configId) //
        );

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update-1.2.json", getClass(), true);
    }

    @Test
    public void test_update_with_james_servers() {
        // Execute the dump
        ResourcesDump resourcesDump = JsonTools.readFromResource("UsageMetricsTest-test_update_with_james_servers-import.json", ResourcesDump.class, getClass());
        JunitsHelper.dumpImport(getCommonServicesContext(), getInternalServicesContext(), resourcesDump);

        // Get the app and assert f2 config contains james
        Application agentF1 = getCommonServicesContext().getResourceService().resourceFindByPk(new Application("usage_agent-f1_example_com")).get();
        Application agentF2 = getCommonServicesContext().getResourceService().resourceFindByPk(new Application("usage_agent-f2_example_com")).get();

        AssertTools.assertJsonComparison("UsageMetricsTest-test_update_with_james_servers-expected.json", getClass(), Arrays.asList(agentF1, agentF2));
    }

    @Test
    public void test_update_with_website() {

        // Create some machines and the mongodb
        InternalChangeService internalChangeService = getInternalServicesContext().getInternalChangeService();
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        Website website = new Website("central");
        website.getDomainNames().add("central.example.com");

        internalChangeService.changesExecute(new ChangesContext(resourceService) //
                .resourceAdd(new Machine("f1.example.com", "127.0.0.1")) //
                .resourceAdd(new Machine("f2.example.com", "127.0.0.2")) //
                .resourceAdd(website) //

                .resourceAdd(new UnixUser(80000L, "mongo", null, null)) //
                .resourceAdd(new MongoDBServer("mongoUsage", "Database for Usage Metrics", "4.0.12", "qwerty")) //
                .linkAdd(new MongoDBServer("mongoUsage"), LinkTypeConstants.RUN_AS, new UnixUser(80000L)) //
                .linkAdd(new MongoDBServer("mongoUsage"), LinkTypeConstants.INSTALLED_ON, new Machine("f1.example.com")) //
                .linkAdd(website, LinkTypeConstants.INSTALLED_ON, new Machine("f1.example.com")) //

        );
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update_with_website-1.json", getClass(), true);

        website = resourceService.resourceFindByPk(website).get();
        Machine machineF1 = resourceService.resourceFindByPk(new Machine("f1.example.com")).get();
        MongoDBServer mongoDbServer = resourceService.resourceFindByPk(new MongoDBServer("mongoUsage")).get();

        // Create the config
        HashMap<String, String> form = new HashMap<>();
        form.put(UsageMetricsConfig.PROPERTY_VERSION, "1.0.0");
        form.put(UsageMetricsConfig.PROPERTY_HOST_KEY_SALT, "aabbcc");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_DATABASE, "mdb");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_USER, "muser");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_PASSWORD, "mpass");
        form.put("mongoDbServer", String.valueOf(mongoDbServer.getInternalId()));
        form.put("centralInstalledOn", String.valueOf(machineF1.getInternalId()));
        form.put("websitesFrom", String.valueOf(website.getInternalId()));

        assertEditorNoErrors(null, new UsageMetricsConfigEditor(), form);

        // Update the UID
        resourceService.resourceFindAll(resourceService.createResourceQuery(UsageMetricsConfig.class)).forEach(umc -> {
            umc.setUid("uid123uid");
            internalChangeService.changesExecute(new ChangesContext(resourceService) //
                    .resourceUpdate(umc) //
            );
        });
        String configId = resourceService.resourceFindByPk(new UsageMetricsConfig("uid123uid")).get().getInternalId();

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update_with_website-2.json", getClass(), true);

        // Change the version
        form = new HashMap<>();
        form.put(UsageMetricsConfig.PROPERTY_VERSION, "2.0.0");
        form.put(UsageMetricsConfig.PROPERTY_HOST_KEY_SALT, "aabbcc");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_DATABASE, "mdb");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_USER, "muser");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_PASSWORD, "mpass");
        form.put("mongoDbServer", String.valueOf(mongoDbServer.getInternalId()));
        form.put("centralInstalledOn", String.valueOf(machineF1.getInternalId()));
        form.put("websitesFrom", String.valueOf(website.getInternalId()));

        assertEditorNoErrors(configId, new UsageMetricsConfigEditor(), form);

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update_with_website-3.json", getClass(), true);

        // Change the salt
        form = new HashMap<>();
        form.put(UsageMetricsConfig.PROPERTY_VERSION, "2.0.0");
        form.put(UsageMetricsConfig.PROPERTY_HOST_KEY_SALT, "ddeeff");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_DATABASE, "mdb");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_USER, "muser");
        form.put(UsageMetricsConfig.PROPERTY_MONGO_PASSWORD, "mpass");
        form.put("mongoDbServer", String.valueOf(mongoDbServer.getInternalId()));
        form.put("centralInstalledOn", String.valueOf(machineF1.getInternalId()));
        form.put("websitesFrom", String.valueOf(website.getInternalId()));

        assertEditorNoErrors(configId, new UsageMetricsConfigEditor(), form);

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update_with_website-4.json", getClass(), true);

        // Add a machine
        internalChangeService.changesExecute(new ChangesContext(resourceService) //
                .resourceAdd(new Machine("f3.example.com", "127.0.0.3")) //
        );

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update_with_website-5.json", getClass(), true);

        // Remove a machine
        internalChangeService.changesExecute(new ChangesContext(resourceService) //
                .resourceDelete(new Machine("f3.example.com")) //
        );

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update_with_website-4.json", getClass(), true);

        // Remove the config (cleanup)
        internalChangeService.changesExecute(new ChangesContext(resourceService) //
                .resourceDelete(configId) //
        );

        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UsageMetricsTest-state-test_update_with_website-1.2.json", getClass(), true);
    }

}
