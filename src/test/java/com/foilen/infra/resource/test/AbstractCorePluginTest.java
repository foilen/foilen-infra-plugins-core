/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.test;

import org.junit.Before;
import org.springframework.test.util.ReflectionTestUtils;

import com.foilen.infra.plugin.core.system.common.service.IPPluginServiceImpl;
import com.foilen.infra.plugin.core.system.fake.ConfigWebUiConfig;
import com.foilen.infra.plugin.core.system.fake.junits.AbstractIPPluginTest;
import com.foilen.infra.plugin.core.system.fake.service.FakeSystemServicesImpl;
import com.foilen.infra.plugin.core.system.fake.service.TranslationServiceImpl;
import com.foilen.infra.plugin.v1.core.common.InfraPluginCommonInit;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.TimerEventContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.TimerEventHandler;
import com.foilen.infra.plugin.v1.core.service.TimerService;
import com.foilen.infra.plugin.v1.core.service.TranslationService;

public class AbstractCorePluginTest extends AbstractIPPluginTest {

    @Override
    @Before
    public void init() {

        fakeSystemServicesImpl = new FakeSystemServicesImpl();
        TimerService timerService = new TimerService() {

            @Override
            public void executeLater(TimerEventHandler eventHandler) {
            }

            @Override
            public void timerAdd(TimerEventContext timer) {
            }
        };

        TranslationService translationService = new TranslationServiceImpl();
        ReflectionTestUtils.setField(translationService, "messageSource", new ConfigWebUiConfig().messageSource());

        CommonServicesContext commonServicesContext = new CommonServicesContext(fakeSystemServicesImpl, new IPPluginServiceImpl(), fakeSystemServicesImpl, timerService, translationService);
        InternalServicesContext internalServicesContext = new InternalServicesContext(fakeSystemServicesImpl, fakeSystemServicesImpl);

        ReflectionTestUtils.setField(fakeSystemServicesImpl, "commonServicesContext", commonServicesContext);
        ReflectionTestUtils.setField(fakeSystemServicesImpl, "internalServicesContext", internalServicesContext);

        InfraPluginCommonInit.init(commonServicesContext, internalServicesContext);

    }

}
