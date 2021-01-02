/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.plugin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.plugin.v1.core.eventhandler.utils.ChangesEventHandlerResourceStream;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.apachephp.ApachePhp;
import com.foilen.infra.resource.composableapplication.ComposableApplication;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.StringTools;
import com.google.common.base.Strings;

public class LetsencryptWithFileAttachableChangesEventHandler extends AbstractBasics implements ChangesEventHandler {

    private static final String NEEDS_REFRESH_PREFIX = "LetsencryptWithFileAttachableChangesEventHandler-needsRefresh-";

    private LetsencryptHelper letsencryptHelper;

    public LetsencryptWithFileAttachableChangesEventHandler() {
        this.letsencryptHelper = new LetsencryptHelperImpl();
    }

    public LetsencryptWithFileAttachableChangesEventHandler(LetsencryptHelper letsencryptHelper) {
        this.letsencryptHelper = letsencryptHelper;
    }

    @Override
    public List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext) {

        List<ActionHandler> actions = new ArrayList<>();

        ChangesEventHandlerResourceStream<LetsEncryptWithFileAttachable> updatedResources = new ChangesEventHandlerResourceStream<>(LetsEncryptWithFileAttachable.class);
        updatedResources.resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources());
        updatedResources.linksAddFrom(changesInTransactionContext.getLastAddedLinks(), LinkTypeConstants.USES, WebsiteCertificate.class);
        updatedResources.linksAddFrom(changesInTransactionContext.getLastDeletedLinks(), LinkTypeConstants.USES, WebsiteCertificate.class);
        updatedResources.linksAddTo(changesInTransactionContext.getLastAddedLinks(), ComposableApplication.LINK_TYPE_ATTACHED);
        updatedResources.linksAddTo(changesInTransactionContext.getLastDeletedLinks(), ComposableApplication.LINK_TYPE_ATTACHED);

        updatedResources.resourcesAdd(new ChangesEventHandlerResourceStream<>(WebsiteCertificate.class) //
                .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources()) //
                .streamFromResourceClassAndLinkType(services, LetsEncryptWithFileAttachable.class, LinkTypeConstants.USES));

        updatedResources.resourcesAdd(new ChangesEventHandlerResourceStream<>(WebsiteCertificate.class) //
                .resourcesAddNextOfType(changesInTransactionContext.getLastUpdatedResources()) //
                .streamFromResourceAndLinkTypeAndToResourceClass(services, ComposableApplication.LINK_TYPE_ATTACHED, LetsEncryptWithFileAttachable.class));

        updatedResources.sortedAndDistinct().getResourcesStream().forEach(letsencryptWithFileAttachable -> {

            logger.info("[{}] Checking", letsencryptWithFileAttachable.getName());

            // When attached, check if needs to update the path
            List<? extends IPResource> attachedLinks = services.getResourceService().linkFindAllByLinkTypeAndToResource(ComposableApplication.LINK_TYPE_ATTACHED, letsencryptWithFileAttachable);

            for (IPResource from : attachedLinks) {

                if (from instanceof ApachePhp) {
                    // Auto fill
                    logger.info("[{}] It is attached to an Apache PHP resource. Check if needs to update the path", letsencryptWithFileAttachable.getName());
                    ApachePhp apachePhp = (ApachePhp) from;
                    String desiredPath;
                    if (Strings.isNullOrEmpty(apachePhp.getMainSiteRelativePath())) {
                        desiredPath = null;
                    } else {
                        desiredPath = DirectoryTools.pathTrailingSlash("/base/" + apachePhp.getMainSiteRelativePath()).replaceAll("//", "/");
                    }
                    String actualPath = letsencryptWithFileAttachable.getBasePath();
                    logger.info("[{}] Desired base path: {} ; actual: {}", letsencryptWithFileAttachable.getName(), desiredPath, actualPath);
                    if (!StringTools.safeEquals(desiredPath, actualPath)) {
                        actions.add((s, changes) -> {
                            logger.info("[{}] Updating path to {}", letsencryptWithFileAttachable.getName(), desiredPath);
                            letsencryptWithFileAttachable.setBasePath(desiredPath);
                            changes.resourceUpdate(letsencryptWithFileAttachable);
                        });
                    }
                } else if (from instanceof ComposableApplication) {
                    // Validate mandatory
                    logger.info("[{}] It is not attached to an Apache PHP resource. Check if has a path", letsencryptWithFileAttachable.getName());
                    if (Strings.isNullOrEmpty(letsencryptWithFileAttachable.getBasePath())) {
                        throw new IllegalUpdateException("Letsencrypt with file attachable [" + letsencryptWithFileAttachable.getName()
                                + "] does not have a base path and is not attache to an Apache PHP resource to automatically get it");
                    }
                } else {
                    throw new IllegalUpdateException("Letsencrypt with file attachable [" + letsencryptWithFileAttachable.getName() + "] is attached to a resource of type [" + from.getClass()
                            + "], but it must be of type ApachePhp or ComposableApplication");
                }

            }

            // Check certs to update when near expiration and well attached
            List<WebsiteCertificate> certsLinks = services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(letsencryptWithFileAttachable, LinkTypeConstants.USES,
                    WebsiteCertificate.class);
            Date expiration = DateTools.addDate(new Date(), Calendar.WEEK_OF_YEAR, 3);
            certsLinks.stream() //
                    .filter(cert -> StringTools.safeEquals(cert.getResourceEditorName(), LetsEncryptWebsiteCertificateEditor.EDITOR_NAME) && DateTools.isAfter(expiration, cert.getEnd())) //
                    .forEach(cert -> {
                        String thumbprint = cert.getThumbprint();
                        logger.info("Certificate {} needs refresh", thumbprint);
                        changesInTransactionContext.getVars().put(NEEDS_REFRESH_PREFIX + thumbprint, thumbprint);
                    });

        });

        // When attached, check that the web certificate is for Lets Encrypt
        changesInTransactionContext.getLastAddedLinks().stream() //
                .filter(link -> link.getA() instanceof LetsEncryptWithFileAttachable) //
                .filter(link -> StringTools.safeEquals(LinkTypeConstants.USES, link.getB())) //
                .filter(link -> link.getC() instanceof WebsiteCertificate) //
                .forEach(link -> {
                    LetsEncryptWithFileAttachable letsencryptWithFileAttachable = (LetsEncryptWithFileAttachable) link.getA();
                    WebsiteCertificate websiteCertificate = (WebsiteCertificate) link.getC();
                    if (!StringTools.safeEquals(websiteCertificate.getResourceEditorName(), LetsEncryptWebsiteCertificateEditor.EDITOR_NAME)) {
                        logger.info("[{}] The used certificate is not a Lets Encrypt certificate", letsencryptWithFileAttachable.getName());
                        throw new IllegalUpdateException(
                                "Letsencrypt with file attachable [" + letsencryptWithFileAttachable.getName() + "] is using a certificate that is not a Lets Encrypt certificate");
                    }
                });

        // Update the certs if needed
        if (changesInTransactionContext.hasChangesInLastRun()) {
            logger.info("hasChangesInLastRun. Skipping for now");
        } else {
            logger.info("There were no changes in last run. Checking if needs a refresh");

            List<WebsiteCertificate> certificatesToUpdate = new ArrayList<>();

            Iterator<Entry<String, String>> it = changesInTransactionContext.getVars().entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> next = it.next();
                if (next.getKey().startsWith(NEEDS_REFRESH_PREFIX)) {
                    Optional<WebsiteCertificate> cert = services.getResourceService().resourceFindByPk(new WebsiteCertificate(next.getValue()));
                    if (cert.isPresent()) {
                        certificatesToUpdate.add(cert.get());
                    }
                    it.remove();
                }
            }

            if (!certificatesToUpdate.isEmpty()) {
                logger.info("Got {} certificates to update", certificatesToUpdate.size());

                actions.add((s, changes) -> {
                    letsencryptHelper.createChallengesAndCreateTimer(services, changes, certificatesToUpdate);
                });
            }
        }

        return actions;
    }

}
