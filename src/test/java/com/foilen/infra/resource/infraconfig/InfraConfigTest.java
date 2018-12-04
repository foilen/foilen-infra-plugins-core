/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.infraconfig;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.core.system.junits.ResourcesDump;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
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
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "InfraConfigTest_test_basic-state.json", getClass(), true);
    }

}
