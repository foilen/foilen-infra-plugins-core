/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.handlers;

import java.util.HashSet;
import java.util.Set;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractCommonMethodUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.CommonMethodUpdateEventHandlerContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.email.resources.EmailAccount;
import com.foilen.infra.resource.email.resources.EmailDomain;
import com.foilen.infra.resource.email.resources.EmailRedirection;
import com.foilen.infra.resource.email.resources.EmailServer;

public class EmailDomainManageDomainsEventHandler extends AbstractCommonMethodUpdateEventHandler<EmailDomain> {

    @Override
    protected void commonHandlerExecute(CommonServicesContext services, ChangesContext changes, CommonMethodUpdateEventHandlerContext<EmailDomain> context) {

        EmailDomain emailDomain = context.getResource();

        // Ensure all account names are unique per account and per redirection (can be shared)
        Set<String> accountNames = new HashSet<>();
        services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(EmailAccount.class, LinkTypeConstants.INSTALLED_ON, emailDomain).forEach(account -> {
            if (!accountNames.add(account.getAccountName())) {
                throw new IllegalUpdateException("The email account [" + account.getAccountName() + "@" + emailDomain.getDomainName() + "] exists multiple times");
            }
        });
        accountNames.clear();
        services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(EmailRedirection.class, LinkTypeConstants.INSTALLED_ON, emailDomain).forEach(redirection -> {
            if (!accountNames.add(redirection.getAccountName())) {
                throw new IllegalUpdateException("The email redirection [" + redirection.getAccountName() + "@" + emailDomain.getDomainName() + "] exists multiple times");
            }
        });

        // Ensure only linked to one EmailServer
        if (services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(emailDomain, LinkTypeConstants.INSTALLED_ON, EmailServer.class).size() > 1) {
            throw new IllegalUpdateException("The email domain [" + emailDomain.getDomainName() + "] is installed on more than one Email server");
        }

    }

    @Override
    public Class<EmailDomain> supportedClass() {
        return EmailDomain.class;
    }

}
