/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.core.system.junits.ResourcesDump;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.global.UpgraderItem;
import com.foilen.infra.resource.letsencrypt.plugin.LetsencryptConfig;
import com.foilen.infra.resource.mariadb.MariaDBServer;
import com.foilen.infra.resource.mongodb.MongoDBServer;
import com.foilen.infra.resource.postgresql.PostgreSqlServer;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.unixuser.SystemUnixUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;
import com.google.common.base.Strings;

public class AllResourcesTest extends AbstractCorePluginTest {

    private Map<String, Long> usersByIp = new HashMap<>();

    public AllResourcesTest() {
        long nextUserId = 71001;
        usersByIp.put("bind9", nextUserId++);
        usersByIp.put("gitlab_postgresql", nextUserId++);
        usersByIp.put("infra_login", nextUserId++);
        usersByIp.put("infra_login_db", nextUserId++);
        usersByIp.put("infra_ui", nextUserId++);
        usersByIp.put("infra_ui_db", nextUserId++);
        usersByIp.put("infra_url_redirection", nextUserId++);
        usersByIp.put("infra_web", nextUserId++);
        usersByIp.put("james_server", nextUserId++);
        usersByIp.put("james_server_db", nextUserId++);
        usersByIp.put("shop_wordpress_db", nextUserId++);
        usersByIp.put("shop_wp", nextUserId++);
        usersByIp.put("ticket-java", nextUserId++);
        usersByIp.put("ticket_db", nextUserId++);

    }

    private void cleanup() {
        // Change all dynamic values
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        process(UpgraderItem.class, it -> {
            changes.resourceDelete(it);
        });
        process(LetsencryptConfig.class, it -> {
            if (!Strings.isNullOrEmpty(it.getAccountKeypairPem())) {
                it.setAccountKeypairPem("KEY_PAIR");
                it.setTagName("letsencrypt_TAG");
                changes.resourceUpdate(it);
            }
        });
        process(MariaDBServer.class, it -> {
            if (!Strings.isNullOrEmpty(it.getRootPassword())) {
                it.setRootPassword("ROOT_PASS");
                changes.resourceUpdate(it);
            }
        });
        process(MongoDBServer.class, it -> {
            if (!Strings.isNullOrEmpty(it.getRootPassword())) {
                it.setRootPassword("ROOT_PASS");
                changes.resourceUpdate(it);
            }
        });
        process(PostgreSqlServer.class, it -> {
            if (!Strings.isNullOrEmpty(it.getRootPassword())) {
                it.setRootPassword("ROOT_PASS");
                changes.resourceUpdate(it);
            }
        });
        process(WebsiteCertificate.class, it -> {
            if (!Strings.isNullOrEmpty(it.getCertificate())) {
                it.setCertificate("CERTIFICATE");
                changes.resourceUpdate(it);
            }
            if (!Strings.isNullOrEmpty(it.getPublicKey())) {
                it.setPublicKey("PUBLIC_KEY");
                changes.resourceUpdate(it);
            }
            if (!Strings.isNullOrEmpty(it.getPrivateKey())) {
                it.setPrivateKey("PRIVATE_KEY");
                changes.resourceUpdate(it);
            }
        });
        process(UnixUser.class, it -> {
            if (it instanceof SystemUnixUser) {
                return;
            }
            Long desiredId = usersByIp.get(it.getName());
            AssertTools.assertNotNull(desiredId, "Didn't provide an id for user " + it.getName());
            if (!desiredId.equals(it.getId())) {
                it.setId(desiredId);
                changes.resourceUpdate(it);
            }
        });
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        process(Application.class, it -> {
            if (StringTools.safeEquals("dns_server_bind9", it.getResourceName())) {
                // Change serial
                SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
                String currentSerial = sdf.format(new Date()) + "00";
                String newSerial = "1111111100";
                String json = JsonTools.compactPrint(it.getApplicationDefinition());
                it.setApplicationDefinition(JsonTools.readFromString(json.replaceAll(currentSerial, newSerial), IPApplicationDefinition.class));
                changes.resourceUpdate(it);
            }
        });
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        removeBinaryFiles();
    }

    private <T extends IPResource> void process(Class<T> type, Consumer<T> consumer) {
        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        resourceService.resourceFindAll(resourceService.createResourceQuery(type)).forEach(it -> consumer.accept(it));
    }

    private void removeBinaryFiles() {
        getInternalServicesContext().getInternalIPResourceService().resourceFindAll().stream() //
                .filter(it -> it instanceof Application) //
                .forEach(it -> {
                    Application application = (Application) it;
                    application.getApplicationDefinition().getAssetsBundles().forEach(bundle -> {
                        bundle.getAssetsRelativePathAndBinaryContent().forEach(file -> file.setB(new byte[] {}));
                    });
                });
    }

    @Test
    public void test_all() {

        fakeSystemServicesImpl.setInfiniteLoopTimeoutInMs(60000);

        // Initial import
        ResourcesDump resourcesDump = JsonTools.readFromResource("AllResourcesTest-initial-import.json", ResourcesDump.class, getClass());
        JunitsHelper.dumpImport(getCommonServicesContext(), getInternalServicesContext(), resourcesDump);

        // Assert
        cleanup();
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "AllResourcesTest-state-1.json", getClass(), true);

    }

}
