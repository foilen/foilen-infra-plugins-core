/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;

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
import com.foilen.smalltools.hash.HashMd5sum;

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

    protected void unrandomizeUids() {
        Set<String> allUids = new HashSet<>();
        resourceServicesInMemoryImpl.getResources().forEach(r -> {
            try {
                BeanWrapper wrapper = new BeanWrapperImpl(r);
                Object normalUid = wrapper.getPropertyValue("uid");
                if (normalUid != null) {
                    String hashUid = HashMd5sum.hashString(r.getClass() + r.getResourceName() + r.getResourceDescription());
                    Assert.assertTrue("The generated uid is not unique. Ensure you have a different description if you have the same name", allUids.add(hashUid));
                    wrapper.setPropertyValue("uid", hashUid);
                }
            } catch (InvalidPropertyException e) {
                // No UID value
            }
        });
    }

}
