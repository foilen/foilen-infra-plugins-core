/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.handlers;

import org.junit.Test;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.email.resources.EmailAccount;
import com.foilen.infra.resource.email.resources.EmailDomain;
import com.foilen.infra.resource.email.resources.EmailRedirection;
import com.foilen.infra.resource.test.AbstractCorePluginTest;

public class EmailDomainManageDomainsEventHandlerTest extends AbstractCorePluginTest {

    @Test(expected = IllegalUpdateException.class)
    public void testValidateUniqueAccounts_2accountsSame_FAIL() {

        // Create domain
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        EmailDomain emailDomain = new EmailDomain();
        emailDomain.setDomainName("example.com");
        emailDomain.setImapDomainName("imap.example.com");
        emailDomain.setPop3DomainName("pop3.example.com");
        changes.resourceAdd(emailDomain);

        // Create accounts
        EmailAccount emailAccount = new EmailAccount();
        emailAccount.setAccountName("a1");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain);
        emailAccount = new EmailAccount();
        emailAccount.setAccountName("a2");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain);
        emailAccount = new EmailAccount();
        emailAccount.setAccountName("a1");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain);

        // Create redirections
        EmailRedirection emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r1");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain);
        emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r2");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

    @Test
    public void testValidateUniqueAccounts_2accountsSameButOnDifferenDomains_OK() {

        // Create domain
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        EmailDomain emailDomain1 = new EmailDomain();
        emailDomain1.setDomainName("example.com");
        emailDomain1.setImapDomainName("imap.example.com");
        emailDomain1.setPop3DomainName("pop3.example.com");
        changes.resourceAdd(emailDomain1);
        EmailDomain emailDomain2 = new EmailDomain();
        emailDomain2.setDomainName("example2.com");
        emailDomain2.setImapDomainName("imap.example2.com");
        emailDomain2.setPop3DomainName("pop3.example2.com");
        changes.resourceAdd(emailDomain2);

        // Create accounts
        EmailAccount emailAccount = new EmailAccount();
        emailAccount.setAccountName("a1");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain1);
        emailAccount = new EmailAccount();
        emailAccount.setAccountName("a2");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain1);
        emailAccount = new EmailAccount();
        emailAccount.setAccountName("a1");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain2);

        // Create redirections
        EmailRedirection emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r1");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain1);
        emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r2");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain1);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

    @Test(expected = IllegalUpdateException.class)
    public void testValidateUniqueAccounts_2redirectionSame_FAIL() {

        // Create domain
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        EmailDomain emailDomain = new EmailDomain();
        emailDomain.setDomainName("example.com");
        emailDomain.setImapDomainName("imap.example.com");
        emailDomain.setPop3DomainName("pop3.example.com");
        changes.resourceAdd(emailDomain);

        // Create accounts
        EmailAccount emailAccount = new EmailAccount();
        emailAccount.setAccountName("a1");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain);
        emailAccount = new EmailAccount();
        emailAccount.setAccountName("a2");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain);

        // Create redirections
        EmailRedirection emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r1");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain);
        emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r2");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain);
        emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r1");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

    @Test
    public void testValidateUniqueAccounts_2redirectionSameButOnDifferenDomains_OK() {

        // Create domain
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        EmailDomain emailDomain1 = new EmailDomain();
        emailDomain1.setDomainName("example.com");
        emailDomain1.setImapDomainName("imap.example.com");
        emailDomain1.setPop3DomainName("pop3.example.com");
        changes.resourceAdd(emailDomain1);
        EmailDomain emailDomain2 = new EmailDomain();
        emailDomain2.setDomainName("example2.com");
        emailDomain2.setImapDomainName("imap.example2.com");
        emailDomain2.setPop3DomainName("pop3.example2.com");
        changes.resourceAdd(emailDomain2);

        // Create accounts
        EmailAccount emailAccount = new EmailAccount();
        emailAccount.setAccountName("a1");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain1);
        emailAccount = new EmailAccount();
        emailAccount.setAccountName("a2");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain1);

        // Create redirections
        EmailRedirection emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r1");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain1);
        emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r2");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain1);
        emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r1");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain2);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

    @Test
    public void testValidateUniqueAccounts_OK() {

        // Create domain
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        EmailDomain emailDomain = new EmailDomain();
        emailDomain.setDomainName("example.com");
        emailDomain.setImapDomainName("imap.example.com");
        emailDomain.setPop3DomainName("pop3.example.com");
        changes.resourceAdd(emailDomain);

        // Create accounts
        EmailAccount emailAccount = new EmailAccount();
        emailAccount.setAccountName("a1");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain);
        emailAccount = new EmailAccount();
        emailAccount.setAccountName("a2");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain);

        // Create redirections
        EmailRedirection emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r1");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain);
        emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r2");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

    @Test
    public void testValidateUniqueAccounts_sameAccountAndRedirection_OK() {

        // Create domain
        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        EmailDomain emailDomain = new EmailDomain();
        emailDomain.setDomainName("example.com");
        emailDomain.setImapDomainName("imap.example.com");
        emailDomain.setPop3DomainName("pop3.example.com");
        changes.resourceAdd(emailDomain);

        // Create accounts
        EmailAccount emailAccount = new EmailAccount();
        emailAccount.setAccountName("a1");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain);
        emailAccount = new EmailAccount();
        emailAccount.setAccountName("a2");
        changes.resourceAdd(emailAccount);
        changes.linkAdd(emailAccount, LinkTypeConstants.INSTALLED_ON, emailDomain);

        // Create redirections
        EmailRedirection emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("a1");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain);
        emailRedirection = new EmailRedirection();
        emailRedirection.setAccountName("r2");
        emailRedirection.getRedirectTos().add("a1@example.com");
        changes.resourceAdd(emailRedirection);
        changes.linkAdd(emailRedirection, LinkTypeConstants.INSTALLED_ON, emailDomain);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

    }

}
