/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerUtils;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.composableapplication.util.AttachablePartUpdatedUtils;
import com.foilen.infra.resource.email.resources.EmailAccount;
import com.foilen.infra.resource.email.resources.EmailDomain;
import com.foilen.infra.resource.email.resources.EmailRedirection;
import com.foilen.infra.resource.email.resources.EmailRelay;
import com.foilen.infra.resource.email.resources.EmailServer;
import com.foilen.infra.resource.email.resources.JamesEmailServer;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StreamTools;

public class JamesEmailServerChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {
        List<ActionHandler> actions = new ArrayList<>();

        // Ensure all account names are unique per account and per redirection (can be shared)
        StreamTools.concat( //
                // EmailAccount -> INSTALLED_ON -> EmailDomain added
                ChangesEventHandlerUtils.getToResourcesStream(changesInTransactionContext.getLastAddedLinks(), EmailAccount.class, LinkTypeConstants.INSTALLED_ON, EmailDomain.class), //
                // EmailRedirection -> INSTALLED_ON -> EmailDomain added
                ChangesEventHandlerUtils.getToResourcesStream(changesInTransactionContext.getLastAddedLinks(), EmailRedirection.class, LinkTypeConstants.INSTALLED_ON, EmailDomain.class), //

                // EmailAccount changed
                ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), EmailAccount.class).map(it -> (EmailAccount) it.getNext()) //
                        .flatMap(emailAccount -> services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(emailAccount, LinkTypeConstants.INSTALLED_ON, EmailDomain.class)
                                .stream()), //

                // EmailRedirection changed
                ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), EmailRedirection.class).map(it -> (EmailRedirection) it.getNext()) //
                        .flatMap(emailRedirection -> services.getResourceService()
                                .linkFindAllByFromResourceAndLinkTypeAndToResourceClass(emailRedirection, LinkTypeConstants.INSTALLED_ON, EmailDomain.class).stream()) //

        ) //
                .map(it -> it.getDomainName()) //
                .sorted().distinct() //
                .forEach(domainName -> {

                    logger.info("Processing domain {}", domainName);

                    IPResourceService resourceService = services.getResourceService();
                    Optional<EmailDomain> o = resourceService.resourceFindByPk(new EmailDomain(domainName));
                    if (!o.isPresent()) {
                        logger.info("{} is not present. Skipping", domainName);
                        return;
                    }
                    EmailDomain emailDomain = o.get();

                    Set<String> accountNames = new HashSet<>();
                    resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(EmailAccount.class, LinkTypeConstants.INSTALLED_ON, emailDomain).forEach(account -> {
                        if (!accountNames.add(account.getAccountName())) {
                            throw new IllegalUpdateException("The email account [" + account.getAccountName() + "@" + emailDomain.getDomainName() + "] exists multiple times");
                        }
                    });
                    accountNames.clear();
                    resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(EmailRedirection.class, LinkTypeConstants.INSTALLED_ON, emailDomain).forEach(redirection -> {
                        if (!accountNames.add(redirection.getAccountName())) {
                            throw new IllegalUpdateException("The email redirection [" + redirection.getAccountName() + "@" + emailDomain.getDomainName() + "] exists multiple times");
                        }
                    });

                });

        // Ensure EmailDomain only linked to one EmailServer
        ChangesEventHandlerUtils.getFromResourcesStream(changesInTransactionContext.getLastAddedLinks(), EmailDomain.class, LinkTypeConstants.INSTALLED_ON, EmailServer.class) //
                .map(it -> it.getDomainName()) //
                .sorted().distinct() //
                .forEach(domainName -> {

                    logger.info("Processing domain {}", domainName);

                    IPResourceService resourceService = services.getResourceService();
                    Optional<EmailDomain> o = resourceService.resourceFindByPk(new EmailDomain(domainName));
                    if (!o.isPresent()) {
                        logger.info("{} is not present. Skipping", domainName);
                        return;
                    }
                    EmailDomain emailDomain = o.get();

                    if (resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(emailDomain, LinkTypeConstants.INSTALLED_ON, EmailServer.class).size() > 1) {
                        throw new IllegalUpdateException("The email domain [" + emailDomain.getDomainName() + "] is installed on more than one Email server");
                    }

                });

        // Manage DNS per EmailDomain
        StreamTools.concat( //
                // EmailDomain added or updated
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), EmailDomain.class), //
                ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), EmailDomain.class).map(it -> (EmailDomain) it.getNext()), //

                // EmailDomain -> INSTALLED_ON -> EmailServer added or deleted
                ChangesEventHandlerUtils.getFromResourcesStream(changesInTransactionContext.getLastAddedLinks(), EmailDomain.class, LinkTypeConstants.INSTALLED_ON, EmailServer.class), //
                ChangesEventHandlerUtils.getFromResourcesStream(changesInTransactionContext.getLastDeletedLinks(), EmailDomain.class, LinkTypeConstants.INSTALLED_ON, EmailServer.class), //

                // EmailServer -> INSTALLED_ON -> Machine added or deleted
                StreamTools.concat( //
                        ChangesEventHandlerUtils.getFromResourcesStream(changesInTransactionContext.getLastAddedLinks(), EmailServer.class, LinkTypeConstants.INSTALLED_ON, Machine.class), //
                        ChangesEventHandlerUtils.getFromResourcesStream(changesInTransactionContext.getLastDeletedLinks(), EmailServer.class, LinkTypeConstants.INSTALLED_ON, Machine.class) //
                ) //
                        .flatMap(emailServer -> services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(EmailDomain.class, LinkTypeConstants.INSTALLED_ON, emailServer)
                                .stream())) //
                .map(it -> it.getDomainName()) //
                .sorted().distinct() //
                .forEach(domainName -> actions.add(new EmailDomainManageDnsEntryActionHandler(domainName)));

        // Manage James Application
        StreamTools.concat( //
                // JamesEmailServer added or updated
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastAddedResources(), JamesEmailServer.class), //
                ChangesEventHandlerUtils.getResourcesOfTypeStream(changesInTransactionContext.getLastRefreshedResources(), JamesEmailServer.class), //
                ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), JamesEmailServer.class).map(it -> (JamesEmailServer) it.getNext()), //

                // Any link on JamesEmailServer
                ChangesEventHandlerUtils.getFromAndToResourcesStream(changesInTransactionContext.getLastAddedLinks(), JamesEmailServer.class), //
                ChangesEventHandlerUtils.getFromAndToResourcesStream(changesInTransactionContext.getLastDeletedLinks(), JamesEmailServer.class), //

                AttachablePartUpdatedUtils.lastChanges(services, changesInTransactionContext, JamesEmailServer.class), //

                StreamTools.concat( //
                        // Any link on EmailDomain
                        ChangesEventHandlerUtils.getFromAndToResourcesStream(changesInTransactionContext.getLastAddedLinks(), EmailDomain.class), //
                        ChangesEventHandlerUtils.getFromAndToResourcesStream(changesInTransactionContext.getLastDeletedLinks(), EmailDomain.class), //

                        // EmailDomain updated
                        ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), EmailDomain.class).map(it -> (EmailDomain) it.getNext()), //

                        // WebsiteCertificate on EmailDomain updated
                        ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), WebsiteCertificate.class) //
                                .map(it -> (WebsiteCertificate) it.getNext()) //
                                .flatMap(websiteCertificate -> StreamTools.concat( //
                                        services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(EmailDomain.class, "USES_SMTP", websiteCertificate).stream(),
                                        services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(EmailDomain.class, "USES_IMAP", websiteCertificate).stream(),
                                        services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(EmailDomain.class, "USES_POP3", websiteCertificate).stream() //
                                )), //

                        // EmailRelay on EmailDomain updated
                        ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), EmailRelay.class) //
                                .map(it -> (EmailRelay) it.getNext()) //
                                .flatMap(emailRelay -> services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(EmailDomain.class, "SEND_THROUGHT", emailRelay).stream()), //

                        // EmailRedirection updated
                        ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), EmailRedirection.class) //
                                .map(it -> (EmailRedirection) it.getNext()) //
                                .flatMap(emailRedirection -> services.getResourceService()
                                        .linkFindAllByFromResourceAndLinkTypeAndToResourceClass(emailRedirection, LinkTypeConstants.INSTALLED_ON, EmailDomain.class).stream()), //

                        // EmailAccount updated
                        ChangesEventHandlerUtils.getNextResourcesOfTypeStream(changesInTransactionContext.getLastUpdatedResources(), EmailAccount.class) //
                                .map(it -> (EmailAccount) it.getNext()) //
                                .flatMap(emailAccount -> services.getResourceService()
                                        .linkFindAllByFromResourceAndLinkTypeAndToResourceClass(emailAccount, LinkTypeConstants.INSTALLED_ON, EmailDomain.class).stream()) //

                ) //
                        .flatMap(emailDomain -> services.getResourceService()
                                .linkFindAllByFromResourceAndLinkTypeAndToResourceClass(emailDomain, LinkTypeConstants.INSTALLED_ON, JamesEmailServer.class).stream()) //

        ) //
                .map(it -> it.getName()) //
                .sorted().distinct() //
                .forEach(serverName -> actions.add(new JamesEmailServerActionHandler(serverName)));

        return actions;
    }

}
