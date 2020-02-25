/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.test;

import org.junit.Before;

import com.foilen.infra.plugin.core.system.common.service.IPPluginServiceImpl;
import com.foilen.infra.plugin.core.system.common.service.MessagingServiceLoggerImpl;
import com.foilen.infra.plugin.core.system.common.service.TranslationServiceImpl;
import com.foilen.infra.plugin.core.system.junits.AbstractIPPluginTest;
import com.foilen.infra.plugin.core.system.memory.service.ResourceServicesInMemoryImpl;
import com.foilen.infra.plugin.v1.core.common.InfraPluginCommonInit;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.TimerEventContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.TimerEventHandler;
import com.foilen.infra.plugin.v1.core.service.TimerService;
import com.foilen.infra.plugin.v1.core.service.TranslationService;

public class AbstractCorePluginTest extends AbstractIPPluginTest {

    private CommonServicesContext commonServicesContext;
    private InternalServicesContext internalServicesContext;
    protected ResourceServicesInMemoryImpl resourceServicesInMemoryImpl;

    @Override
    protected CommonServicesContext getCommonServicesContext() {
        return commonServicesContext;
    }

    @Override
    protected InternalServicesContext getInternalServicesContext() {
        return internalServicesContext;
    }

    @Before
    public void init() {

        System.setProperty("PluginUpgrader.disable", "true");

        TimerService timerService = new TimerService() {

            @Override
            public void executeLater(TimerEventHandler eventHandler) {
            }

            @Override
            public void timerAdd(TimerEventContext timer) {
            }
        };
        resourceServicesInMemoryImpl = new ResourceServicesInMemoryImpl();

        TranslationService translationService = new TranslationServiceImpl();

        commonServicesContext = new CommonServicesContext(new MessagingServiceLoggerImpl(), new IPPluginServiceImpl(), resourceServicesInMemoryImpl, timerService, translationService);
        internalServicesContext = new InternalServicesContext(resourceServicesInMemoryImpl, resourceServicesInMemoryImpl);

        resourceServicesInMemoryImpl.setCommonServicesContext(commonServicesContext);
        resourceServicesInMemoryImpl.setInternalServicesContext(internalServicesContext);

        InfraPluginCommonInit.init(commonServicesContext, internalServicesContext);

    }

}
