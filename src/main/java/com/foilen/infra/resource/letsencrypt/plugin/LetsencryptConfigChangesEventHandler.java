/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.smalltools.crypt.spongycastle.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.spongycastle.asymmetric.RSACrypt;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.StreamTools;
import com.foilen.smalltools.tools.StringTools;
import com.google.common.base.Strings;

public class LetsencryptConfigChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {
        List<ActionHandler> actions = new ArrayList<>();

        StreamTools.concat( //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), LetsencryptConfig.class), //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastRefreshedResources(), LetsencryptConfig.class), //
                ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), LetsencryptConfig.class).map(it -> (LetsencryptConfig) it.getNext()) //
        ) //
                .map(it -> it.getName()) //
                .sorted().distinct() //
                .forEach(name -> {

                    actions.add((s, changes) -> {

                        logger.info("Processing Lets Encrypt config {}", name);

                        IPResourceService resourceService = services.getResourceService();
                        Optional<LetsencryptConfig> o = resourceService.resourceFindByPk(new LetsencryptConfig(name));
                        if (!o.isPresent()) {
                            logger.info("{} is not present. Skipping", name);
                            return;
                        }
                        LetsencryptConfig resource = o.get();

                        // Ensure there is a single config
                        List<LetsencryptConfig> all = resourceService.resourceFindAll(resourceService.createResourceQuery(LetsencryptConfig.class));
                        if (all.stream().anyMatch(it -> !StringTools.safeEquals(it.getName(), name))) {
                            throw new IllegalUpdateException(services.getTranslationService().translate("error.onlyOneConfig"));
                        }

                        boolean update = false;
                        // accountKeypairPem
                        if (Strings.isNullOrEmpty(resource.getAccountKeypairPem())) {
                            logger.info("Generating an AccountKeypair");
                            AsymmetricKeys keys = RSACrypt.RSA_CRYPT.generateKeyPair(4096);
                            String accountPem = RSACrypt.RSA_CRYPT.savePrivateKeyPemAsString(keys) + RSACrypt.RSA_CRYPT.savePublicKeyPemAsString(keys);
                            resource.setAccountKeypairPem(accountPem);
                            update = true;
                        }

                        // tagName
                        if (Strings.isNullOrEmpty(resource.getTagName())) {
                            logger.info("Generating a Tag name");
                            resource.setTagName("letsencrypt_" + SecureRandomTools.randomHexString(10).toLowerCase());
                            update = true;
                        }

                        // Update if changed
                        if (update) {
                            changes.resourceUpdate(resource);
                        }

                    });

                });

        return actions;
    }

}
