/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.infraconfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.UpdateEventHandler;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tuple.Tuple3;

public class InfraConfigUpdateHandler extends AbstractBasics implements UpdateEventHandler<InfraConfigPlugin> {

    @Override
    public void addHandler(CommonServicesContext services, ChangesContext changes, InfraConfigPlugin resource) {
        refreshInfraConfig(services, changes, resource, null);
    }

    @Override
    public void checkAndFix(CommonServicesContext services, ChangesContext changes, InfraConfigPlugin resource) {
    }

    @Override
    public void deleteHandler(CommonServicesContext services, ChangesContext changes, InfraConfigPlugin resource, List<Tuple3<IPResource, String, IPResource>> previousLinks) {
        refreshInfraConfig(services, changes, resource, previousLinks);
    }

    private void refreshInfraConfig(CommonServicesContext services, ChangesContext changes, InfraConfigPlugin plugin, List<Tuple3<IPResource, String, IPResource>> links) {

        // Get the list of InfraConfig to update
        Set<Long> knownInfraConfigs = new HashSet<>();
        List<InfraConfig> infraConfigs = new ArrayList<>();
        if (links == null) {
            services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(InfraConfig.class, "UI_USES", plugin).stream() //
                    .forEach(ic -> {
                        if (knownInfraConfigs.add(ic.getInternalId())) {
                            infraConfigs.add(ic);
                        }
                    });
        } else {
            links.stream() //
                    .filter(link -> link.getA() instanceof InfraConfig) //
                    .map(link -> (InfraConfig) link.getA()) //
                    .forEach(ic -> {
                        if (knownInfraConfigs.add(ic.getInternalId())) {
                            infraConfigs.add(ic);
                        }
                    });
        }

        // Ask for refresh
        infraConfigs.forEach(ic -> {
            logger.info("Mark {} for refresh", ic.getResourceName());
            changes.resourceRefresh(ic);
        });

    }

    @Override
    public Class<InfraConfigPlugin> supportedClass() {
        return InfraConfigPlugin.class;
    }

    @Override
    public void updateHandler(CommonServicesContext services, ChangesContext changes, InfraConfigPlugin previousResource, InfraConfigPlugin newResource) {
        refreshInfraConfig(services, changes, newResource, null);
    }

}
