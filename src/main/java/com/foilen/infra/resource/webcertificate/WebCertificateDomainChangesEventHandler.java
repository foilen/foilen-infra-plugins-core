/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.webcertificate;

import java.util.ArrayList;
import java.util.List;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.resource.domain.DomainResourceHelper;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StreamTools;

public class WebCertificateDomainChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        List<ActionHandler> actions = new ArrayList<>();

        StreamTools.concat( //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), WebsiteCertificate.class), //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastRefreshedResources(), WebsiteCertificate.class), //
                ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), WebsiteCertificate.class).map(it -> (WebsiteCertificate) it.getNext()) //
        ) //
                .forEach(websiteCertificate -> {
                    actions.add((s, changes) -> DomainResourceHelper.syncManagedLinks(s, changes, websiteCertificate, websiteCertificate.getDomainNames()));
                });

        return actions;
    }

}
