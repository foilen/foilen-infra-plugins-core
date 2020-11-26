/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.infraconfig;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.core.system.junits.ResourcesDump;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.JsonTools;

public class InfraConfigTest extends AbstractCorePluginTest {

    @Test
    public void test_basic() {

        // Execute the dump
        ResourcesDump resourcesDump = JsonTools.readFromResource("InfraConfigTest_test_basic-import.json", ResourcesDump.class, getClass());
        JunitsHelper.dumpImport(getCommonServicesContext(), getInternalServicesContext(), resourcesDump);

        // Remove the binary files that are always changing
        getInternalServicesContext().getInternalIPResourceService().resourceFindAll().stream() //
                .filter(it -> it instanceof Application) //
                .forEach(it -> {
                    Application application = (Application) it;
                    application.getApplicationDefinition().getAssetsBundles().forEach(bundle -> {
                        bundle.getAssetsRelativePathAndBinaryContent().forEach(file -> file.setB(new byte[] {}));
                    });
                });

        // Assert
        unrandomizeUids();
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "InfraConfigTest_test_basic-state.json", getClass(), true);

    }

    @Test
    public void test_basic_update_login_cert() {

        // Initial
        test_basic();

        // Update certificate
        WebsiteCertificate newWebsiteCertificate = JsonTools.readFromResource("test_basic_update_login_cert-new_login_cert.json", WebsiteCertificate.class, getClass());
        WebsiteCertificate websiteCertificate = getCommonServicesContext().getResourceService().resourceFindByPk(new WebsiteCertificate("3db0fbf94792b7eb21db1652e0566f7d2b5904e9")).get();
        getInternalServicesContext().getInternalChangeService().changesExecute(new ChangesContext(getCommonServicesContext().getResourceService()) //
                .resourceUpdate(websiteCertificate, newWebsiteCertificate));

        // Assert
        unrandomizeUids();
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "test_basic_update_login_cert-after-cert-update-state.json", getClass(), true);

    }

    @Test
    public void test_basic_update_plugin_url() {

        // Initial
        test_basic();

        // Change url
        Optional<InfraConfigPlugin> pluginOptional = getCommonServicesContext().getResourceService()
                .resourceFind(getCommonServicesContext().getResourceService().createResourceQuery(InfraConfigPlugin.class) //
                        .propertyEquals(InfraConfigPlugin.PROPERTY_URL, "https://dl.bintray.com/foilen/maven/com/foilen/foilen-infra-resource-dns/0.1.0/foilen-infra-resource-dns-0.1.0.jar") //
                );
        Assert.assertTrue(pluginOptional.isPresent());
        InfraConfigPlugin plugin = pluginOptional.get();
        InfraConfigPlugin newPlugin = JsonTools.clone(plugin);
        newPlugin.setUrl("https://dl.bintray.com/foilen/maven/com/foilen/foilen-infra-resource-dns/0.2.0/foilen-infra-resource-dns-0.2.0.jar");
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceUpdate(plugin, newPlugin);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Check
        unrandomizeUids();
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "InfraConfigTest_test_basic_update_plugin_url-state.json", getClass(), true);
    }

    @Test
    public void test_migration() {

        // Execute the dump
        ResourcesDump resourcesDump = JsonTools.readFromResource("InfraConfigTest_test_migration-import.json", ResourcesDump.class, getClass());
        JunitsHelper.dumpImport(getCommonServicesContext(), getInternalServicesContext(), resourcesDump);

        // Remove the binary files that are always changing
        getInternalServicesContext().getInternalIPResourceService().resourceFindAll().stream() //
                .filter(it -> it instanceof Application) //
                .forEach(it -> {
                    Application application = (Application) it;
                    application.getApplicationDefinition().getAssetsBundles().forEach(bundle -> {
                        bundle.getAssetsRelativePathAndBinaryContent().forEach(file -> file.setB(new byte[] {}));
                    });
                });

        // Assert
        unrandomizeUids();
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "InfraConfigTest_test_migration-state.json", getClass(), true);

    }

}
