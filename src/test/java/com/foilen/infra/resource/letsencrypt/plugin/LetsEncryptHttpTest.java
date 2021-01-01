/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.plugin;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.core.system.junits.ResourcesDump;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.apachephp.ApachePhp;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.letsencrypt.acme.mock.AcmeServiceMock;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.JsonTools;

public class LetsEncryptHttpTest extends AbstractCorePluginTest {

    @Test
    public void test_php_deeper() {

        LetsencryptHelperImpl.setAcmeServiceGenerator(config -> new AcmeServiceMock());

        IPResourceService resourceService = getCommonServicesContext().getResourceService();

        // Create PHP website
        ResourcesDump resourcesDump = JsonTools.readFromResource("LetsEncryptHttpTest-test_php_deeper-initial-import.json", ResourcesDump.class, getClass());
        JunitsHelper.dumpImport(getCommonServicesContext(), getInternalServicesContext(), resourcesDump);

        Application testing = resourceService.resourceFindByPk(new Application("testing")).get();
        AssertTools.assertJsonComparison("LetsEncryptHttpTest-test_php_deeper-initial-testing-0.json", getClass(), testing);

        WebsiteCertificate websiteCertificate = resourceService.resourceFindByPk(new WebsiteCertificate("eafd274ff55444c8d69ba59fbb8f258a2271cb06")).get();
        ApachePhp apachePhp = resourceService.resourceFindByPk(new ApachePhp("testing")).get();

        // Create and attach the WithFile
        LetsEncryptWithFileAttachableEditor resourceEditor = new LetsEncryptWithFileAttachableEditor();
        Map<String, String> formValues = new HashMap<>();
        formValues.put(LetsEncryptWithFileAttachable.PROPERTY_NAME, "testing");
        formValues.put("websiteCertificate", websiteCertificate.getInternalId().toString());
        formValues.put("attachedTo", apachePhp.getInternalId().toString());
        assertEditorNoErrors(null, resourceEditor, formValues);

        testing = resourceService.resourceFindByPk(new Application("testing")).get();
        AssertTools.assertJsonComparison("LetsEncryptHttpTest-test_php_deeper-initial-testing-1.json", getClass(), testing);

        // Replay
        LetsEncryptWithFileAttachable letsEncryptWithFileAttachable = resourceService.resourceFindByPk(new LetsEncryptWithFileAttachable("testing")).get();
        formValues = new HashMap<>();
        formValues.put(LetsEncryptWithFileAttachable.PROPERTY_NAME, "testing");
        formValues.put("websiteCertificate", websiteCertificate.getInternalId().toString());
        formValues.put("attachedTo", apachePhp.getInternalId().toString());
        assertEditorNoErrors(letsEncryptWithFileAttachable.getInternalId(), resourceEditor, formValues);

        testing = resourceService.resourceFindByPk(new Application("testing")).get();
        AssertTools.assertJsonComparison("LetsEncryptHttpTest-test_php_deeper-initial-testing-1.json", getClass(), testing);
    }

    @Test
    public void test_php_in_base_path() {

        LetsencryptHelperImpl.setAcmeServiceGenerator(config -> new AcmeServiceMock());

        IPResourceService resourceService = getCommonServicesContext().getResourceService();

        // Create PHP website
        ResourcesDump resourcesDump = JsonTools.readFromResource("LetsEncryptHttpTest-test_php_in_base_path-initial-import.json", ResourcesDump.class, getClass());
        JunitsHelper.dumpImport(getCommonServicesContext(), getInternalServicesContext(), resourcesDump);

        Application testing = resourceService.resourceFindByPk(new Application("testing")).get();
        AssertTools.assertJsonComparison("LetsEncryptHttpTest-test_php_in_base_path-initial-testing-0.json", getClass(), testing);

        WebsiteCertificate websiteCertificate = resourceService.resourceFindByPk(new WebsiteCertificate("eafd274ff55444c8d69ba59fbb8f258a2271cb06")).get();
        ApachePhp apachePhp = resourceService.resourceFindByPk(new ApachePhp("testing")).get();

        // Create and attach the WithFile
        LetsEncryptWithFileAttachableEditor resourceEditor = new LetsEncryptWithFileAttachableEditor();
        Map<String, String> formValues = new HashMap<>();
        formValues.put(LetsEncryptWithFileAttachable.PROPERTY_NAME, "testing");
        formValues.put("websiteCertificate", websiteCertificate.getInternalId().toString());
        formValues.put("attachedTo", apachePhp.getInternalId().toString());
        assertEditorNoErrors(null, resourceEditor, formValues);

        testing = resourceService.resourceFindByPk(new Application("testing")).get();
        AssertTools.assertJsonComparison("LetsEncryptHttpTest-test_php_in_base_path-initial-testing-1.json", getClass(), testing);

        // Replay
        LetsEncryptWithFileAttachable letsEncryptWithFileAttachable = resourceService.resourceFindByPk(new LetsEncryptWithFileAttachable("testing")).get();
        formValues = new HashMap<>();
        formValues.put(LetsEncryptWithFileAttachable.PROPERTY_NAME, "testing");
        formValues.put("websiteCertificate", websiteCertificate.getInternalId().toString());
        formValues.put("attachedTo", apachePhp.getInternalId().toString());
        assertEditorNoErrors(letsEncryptWithFileAttachable.getInternalId(), resourceEditor, formValues);

        testing = resourceService.resourceFindByPk(new Application("testing")).get();
        AssertTools.assertJsonComparison("LetsEncryptHttpTest-test_php_in_base_path-initial-testing-1.json", getClass(), testing);

    }

    @Test
    public void test_php_in_base_path_2_at_a_time() {

        LetsencryptHelperImpl.setAcmeServiceGenerator(config -> new AcmeServiceMock());

        IPResourceService resourceService = getCommonServicesContext().getResourceService();

        // Create PHP website
        ResourcesDump resourcesDump = JsonTools.readFromResource("LetsEncryptHttpTest-test_php_in_base_path_2_at_a_time-initial-import.json", ResourcesDump.class, getClass());
        JunitsHelper.dumpImport(getCommonServicesContext(), getInternalServicesContext(), resourcesDump);

        Application testing = resourceService.resourceFindByPk(new Application("testing")).get();
        AssertTools.assertJsonComparison("LetsEncryptHttpTest-test_php_in_base_path-initial-testing-0.json", getClass(), testing);

        WebsiteCertificate websiteCertificate = resourceService.resourceFindByPk(new WebsiteCertificate("eafd274ff55444c8d69ba59fbb8f258a2271cb06")).get();
        ApachePhp apachePhp = resourceService.resourceFindByPk(new ApachePhp("testing")).get();
        WebsiteCertificate websiteCertificate1 = resourceService.resourceFindByPk(new WebsiteCertificate("0d7ccc46465711ac38afa3db087be2f9793f2d19")).get();

        // Create and attach the WithFile first
        LetsEncryptWithFileAttachableEditor resourceEditor = new LetsEncryptWithFileAttachableEditor();
        Map<String, String> formValues = new HashMap<>();
        formValues.put(LetsEncryptWithFileAttachable.PROPERTY_NAME, "testing");
        formValues.put("websiteCertificate", websiteCertificate.getInternalId().toString());
        formValues.put("attachedTo", apachePhp.getInternalId().toString());
        assertEditorNoErrors(null, resourceEditor, formValues);

        // Create and attach the WithFile second
        resourceEditor = new LetsEncryptWithFileAttachableEditor();
        formValues = new HashMap<>();
        formValues.put(LetsEncryptWithFileAttachable.PROPERTY_NAME, "testing1");
        formValues.put("websiteCertificate", websiteCertificate1.getInternalId().toString());
        formValues.put("attachedTo", apachePhp.getInternalId().toString());
        assertEditorNoErrors(null, resourceEditor, formValues);

        testing = resourceService.resourceFindByPk(new Application("testing")).get();
        AssertTools.assertJsonComparison("LetsEncryptHttpTest-test_php_in_base_path_2_at_a_time-initial-testing-1.json", getClass(), testing);

        // Replay
        LetsEncryptWithFileAttachable letsEncryptWithFileAttachable = resourceService.resourceFindByPk(new LetsEncryptWithFileAttachable("testing")).get();
        formValues = new HashMap<>();
        formValues.put(LetsEncryptWithFileAttachable.PROPERTY_NAME, "testing");
        formValues.put("websiteCertificate", websiteCertificate.getInternalId().toString());
        formValues.put("attachedTo", apachePhp.getInternalId().toString());
        assertEditorNoErrors(letsEncryptWithFileAttachable.getInternalId(), resourceEditor, formValues);

        testing = resourceService.resourceFindByPk(new Application("testing")).get();
        AssertTools.assertJsonComparison("LetsEncryptHttpTest-test_php_in_base_path_2_at_a_time-initial-testing-1.json", getClass(), testing);

    }
}
