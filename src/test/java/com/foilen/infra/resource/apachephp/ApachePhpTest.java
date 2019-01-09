/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.test.AbstractCorePluginTest;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.website.Website;

public class ApachePhpTest extends AbstractCorePluginTest {

    @Test
    public void test_basic() {

        // Create resources
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser infraUrlUnixUser = new UnixUser(70000L, "infra_url_redirection", "/home/infra_url_redirection", null, null);
        UnixUser unixUser = new UnixUser(72000L, "myphp", "/home/myphp", null, null);
        Website website = new Website("myphp");
        website.getDomainNames().add("myphp.example.com");

        ApachePhp apachePhp = new ApachePhp();
        apachePhp.setName("myphp");
        apachePhp.setBasePath("/home/myphp/php");
        apachePhp.setMainSiteRelativePath("/www/");
        apachePhp.setResourceEditorName(ApachePhpEditor.EDITOR_NAME);

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(machine);
        changes.resourceAdd(infraUrlUnixUser);
        changes.resourceAdd(unixUser);
        changes.resourceAdd(apachePhp);
        changes.resourceAdd(website);

        // Create links
        changes.linkAdd(apachePhp, LinkTypeConstants.RUN_AS, unixUser);
        changes.linkAdd(apachePhp, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(website, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(website, LinkTypeConstants.POINTS_TO, apachePhp);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpTest_test_basic-state.json", getClass(), true);
    }

    @Test
    public void test_withBasicAuth() {

        // Create resources
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser infraUrlUnixUser = new UnixUser(70000L, "infra_url_redirection", "/home/infra_url_redirection", null, null);
        UnixUser unixUser = new UnixUser(72000L, "myphp", "/home/myphp", null, null);
        Website website = new Website("myphp");
        website.getDomainNames().add("myphp.example.com");

        ApachePhp apachePhp = new ApachePhp();
        apachePhp.setName("myphp");
        apachePhp.setBasePath("/home/myphp/php");
        apachePhp.setMainSiteRelativePath("/www/");
        apachePhp.setResourceEditorName(ApachePhpEditor.EDITOR_NAME);

        ApachePhpFolder apachePhpFolder = new ApachePhpFolder("/home/myphp/otherphp", "inside", "/other");
        apachePhpFolder.setUid("the_uid");

        ApachePhpHtPasswd passwd1 = new ApachePhpHtPasswd("u1", "a");
        passwd1.setUid("the_uid_1");
        ApachePhpHtPasswd passwd2 = new ApachePhpHtPasswd("u2", "b");
        passwd2.setUid("the_uid_2");

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(machine);
        changes.resourceAdd(infraUrlUnixUser);
        changes.resourceAdd(unixUser);
        changes.resourceAdd(apachePhp);
        changes.resourceAdd(website);
        changes.resourceAdd(apachePhpFolder);
        changes.resourceAdd(passwd1);
        changes.resourceAdd(passwd2);

        // Create links
        changes.linkAdd(apachePhp, LinkTypeConstants.RUN_AS, unixUser);
        changes.linkAdd(apachePhp, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(apachePhp, LinkTypeConstants.USES, apachePhpFolder);
        changes.linkAdd(apachePhp, LinkTypeConstants.USES, passwd1);
        changes.linkAdd(apachePhp, LinkTypeConstants.USES, passwd2);
        changes.linkAdd(website, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(website, LinkTypeConstants.POINTS_TO, apachePhp);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpTest_test_withBasicAuth-state.json", getClass(), true);
    }

    @Test
    public void test_withFolders_1() {

        // Create resources
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser infraUrlUnixUser = new UnixUser(70000L, "infra_url_redirection", "/home/infra_url_redirection", null, null);
        UnixUser unixUser = new UnixUser(72000L, "myphp", "/home/myphp", null, null);
        Website website = new Website("myphp");
        website.getDomainNames().add("myphp.example.com");

        ApachePhp apachePhp = new ApachePhp();
        apachePhp.setName("myphp");
        apachePhp.setBasePath("/home/myphp/php");
        apachePhp.setMainSiteRelativePath("/www/");
        apachePhp.setResourceEditorName(ApachePhpEditor.EDITOR_NAME);

        ApachePhpFolder apachePhpFolder = new ApachePhpFolder("/home/myphp/otherphp", "inside", "/other");
        apachePhpFolder.setUid("the_uid");

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(machine);
        changes.resourceAdd(infraUrlUnixUser);
        changes.resourceAdd(unixUser);
        changes.resourceAdd(apachePhp);
        changes.resourceAdd(website);
        changes.resourceAdd(apachePhpFolder);

        // Create links
        changes.linkAdd(apachePhp, LinkTypeConstants.RUN_AS, unixUser);
        changes.linkAdd(apachePhp, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(apachePhp, LinkTypeConstants.USES, apachePhpFolder);
        changes.linkAdd(website, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(website, LinkTypeConstants.POINTS_TO, apachePhp);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpTest_test_withFolders_1-state.json", getClass(), true);
    }

    @Test
    public void test_withFolders_2() {

        // Create resources
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser infraUrlUnixUser = new UnixUser(70000L, "infra_url_redirection", "/home/infra_url_redirection", null, null);
        UnixUser unixUser = new UnixUser(72000L, "myphp", "/home/myphp", null, null);
        Website website = new Website("myphp");
        website.getDomainNames().add("myphp.example.com");

        ApachePhp apachePhp = new ApachePhp();
        apachePhp.setName("myphp");
        apachePhp.setBasePath("/home/myphp/php");
        apachePhp.setMainSiteRelativePath("/www/");
        apachePhp.setResourceEditorName(ApachePhpEditor.EDITOR_NAME);

        ApachePhpFolder apachePhpFolder1 = new ApachePhpFolder("/home/myphp/otherphp", "inside", "other");
        apachePhpFolder1.setUid("the_uid_1");
        ApachePhpFolder apachePhpFolder2 = new ApachePhpFolder("/home/myphp/otherphp", "/", "/party/");
        apachePhpFolder2.setUid("the_uid_2");

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(machine);
        changes.resourceAdd(infraUrlUnixUser);
        changes.resourceAdd(unixUser);
        changes.resourceAdd(apachePhp);
        changes.resourceAdd(website);
        changes.resourceAdd(apachePhpFolder1);
        changes.resourceAdd(apachePhpFolder2);

        // Create links
        changes.linkAdd(apachePhp, LinkTypeConstants.RUN_AS, unixUser);
        changes.linkAdd(apachePhp, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(apachePhp, LinkTypeConstants.USES, apachePhpFolder1);
        changes.linkAdd(apachePhp, LinkTypeConstants.USES, apachePhpFolder2);
        changes.linkAdd(website, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(website, LinkTypeConstants.POINTS_TO, apachePhp);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpTest_test_withFolders_2-state.json", getClass(), true);
    }

}
