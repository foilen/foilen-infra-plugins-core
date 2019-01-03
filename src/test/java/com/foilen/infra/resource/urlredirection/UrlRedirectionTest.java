/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.urlredirection;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.infra.resource.webcertificate.helper.CertificateHelper;
import com.foilen.smalltools.crypt.spongycastle.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.spongycastle.asymmetric.RSACrypt;
import com.foilen.smalltools.crypt.spongycastle.cert.CertificateDetails;
import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;
import com.foilen.smalltools.tools.ResourceTools;

public class UrlRedirectionTest extends AbstractCorePluginTest {

    public static void main(String[] args) {
        AsymmetricKeys keys = RSACrypt.RSA_CRYPT.generateKeyPair(1024);
        RSACertificate rootCertificate = new RSACertificate(keys);
        rootCertificate.selfSign(new CertificateDetails().setCommonName("redir.example.com"));
        System.out.println(rootCertificate.saveCertificatePemAsString());
        System.out.println(RSACrypt.RSA_CRYPT.savePrivateKeyPemAsString(keys));
        System.out.println(RSACrypt.RSA_CRYPT.savePublicKeyPemAsString(keys));
    }

    @Test
    public void test_exact_sub_directory_perm() {
        testRedirConfig("https://www.example.com/exact", true, "UrlRedirectionTest-test_exact_sub_directory_perm.json");
    }

    @Test
    public void test_exact_sub_directory_temp() {
        testRedirConfig("https://www.example.com/exact", false, "UrlRedirectionTest-test_exact_sub_directory_temp.json");
    }

    @Test
    public void test_http_and_https() {

        // Create resources
        Machine machine1 = new Machine("h1.example.com", "192.168.0.200");

        UrlRedirection urlRedirection = new UrlRedirection();
        urlRedirection.setDomainName("redir.example.com");
        urlRedirection.setHttpRedirectToUrl("http://google.com");
        urlRedirection.setHttpsRedirectToUrl("https://google.com");

        RSACertificate rsaCertificate = RSACertificate.loadPemFromString(ResourceTools.getResourceAsString("cert.pem", getClass()));
        WebsiteCertificate websiteCertificate = CertificateHelper.toWebsiteCertificate(null, rsaCertificate);

        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceAdd(machine1);
        changes.resourceAdd(urlRedirection);
        changes.resourceAdd(websiteCertificate);

        // Create links
        changes.linkAdd(urlRedirection, LinkTypeConstants.INSTALLED_ON, machine1);
        changes.linkAdd(urlRedirection, LinkTypeConstants.USES, websiteCertificate);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UrlRedirectionTest-test_http_and_https-state-01.json", getClass(), true);

        // Add another
        urlRedirection = new UrlRedirection();
        urlRedirection.setDomainName("redir2.example.com");
        urlRedirection.setHttpRedirectToUrl("http://example.com");
        changes.clear();
        changes.resourceAdd(urlRedirection);
        changes.linkAdd(urlRedirection, LinkTypeConstants.INSTALLED_ON, machine1);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UrlRedirectionTest-test_http_and_https-state-02.json", getClass(), true);

        // Remove https
        urlRedirection = resourceService.resourceFind(resourceService.createResourceQuery(UrlRedirection.class).propertyEquals(UrlRedirection.PROPERTY_DOMAIN_NAME, "redir.example.com")).get();
        urlRedirection.setHttpsRedirectToUrl(null);
        changes.clear();
        changes.resourceUpdate(urlRedirection);
        changes.linkDelete(urlRedirection, LinkTypeConstants.USES, websiteCertificate);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UrlRedirectionTest-test_http_and_https-state-03.json", getClass(), true);

        // Add a machine
        Machine machine2 = new Machine("h2.example.com", "192.168.0.202");
        changes.clear();
        changes.resourceAdd(machine2);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UrlRedirectionTest-test_http_and_https-state-04.json", getClass(), true);

        // Install a redirection on the new machine
        changes.clear();
        changes.linkAdd(urlRedirection, LinkTypeConstants.INSTALLED_ON, machine2);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UrlRedirectionTest-test_http_and_https-state-05.json", getClass(), true);

        // Remove the redirection from the new machine
        changes.clear();
        changes.linkDelete(urlRedirection, LinkTypeConstants.INSTALLED_ON, machine2);
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UrlRedirectionTest-test_http_and_https-state-04.json", getClass(), true);

        // Remove all redirections
        changes.clear();
        resourceService.resourceFindAll(resourceService.createResourceQuery(UrlRedirection.class)).forEach(it -> changes.resourceDelete(it));
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "UrlRedirectionTest-test_http_and_https-state-06.json", getClass(), true);

    }

    @Test
    public void test_keep_rest_domain_perm() {
        testRedirConfig("https://www.example.com", true, "UrlRedirectionTest-test_keep_rest_domain_perm.json");
    }

    @Test
    public void test_keep_rest_domain_perm_endSlash() {
        testRedirConfig("https://www.example.com/", true, "UrlRedirectionTest-test_keep_rest_domain_perm_endSlash.json");
    }

    @Test
    public void test_keep_rest_domain_temp() {
        testRedirConfig("https://www.example.com", false, "UrlRedirectionTest-test_keep_rest_domain_temp.json");
    }

    @Test
    public void test_keep_rest_domain_temp_endSlash() {
        testRedirConfig("https://www.example.com/", false, "UrlRedirectionTest-test_keep_rest_domain_temp_endSlash.json");
    }

    @Test
    public void test_keep_rest_sub_directory_perm() {
        testRedirConfig("https://www.example.com/sub/", true, "UrlRedirectionTest-test_keep_rest_sub_directory_perm.json");
    }

    @Test
    public void test_keep_rest_sub_directory_temp() {
        testRedirConfig("https://www.example.com/sub/", false, "UrlRedirectionTest-test_keep_rest_sub_directory_temp.json");
    }

    private void testRedirConfig(String url, boolean isPermanent, String expectedJson) {
        // Create resources
        Machine machine1 = new Machine("h1.example.com", "192.168.0.200");

        UrlRedirection urlRedirection = new UrlRedirection();
        urlRedirection.setDomainName("redir.example.com");
        urlRedirection.setHttpRedirectToUrl(url);
        urlRedirection.setHttpsRedirectToUrl(url);
        urlRedirection.setHttpIsPermanent(isPermanent);
        urlRedirection.setHttpsIsPermanent(isPermanent);

        RSACertificate rsaCertificate = RSACertificate.loadPemFromString(ResourceTools.getResourceAsString("cert.pem", getClass()));
        WebsiteCertificate websiteCertificate = CertificateHelper.toWebsiteCertificate(null, rsaCertificate);

        IPResourceService resourceService = getCommonServicesContext().getResourceService();
        ChangesContext changes = new ChangesContext(resourceService);
        changes.resourceAdd(machine1);
        changes.resourceAdd(urlRedirection);
        changes.resourceAdd(websiteCertificate);

        // Create links
        changes.linkAdd(urlRedirection, LinkTypeConstants.INSTALLED_ON, machine1);
        changes.linkAdd(urlRedirection, LinkTypeConstants.USES, websiteCertificate);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), expectedJson, getClass(), true);

    }

}
