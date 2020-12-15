/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.global.upgrader;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.global.AbstractPluginUpgraderTask;
import com.foilen.infra.resource.letsencrypt.plugin.LetsEncryptWebsiteCertificateEditor;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.ResourceTools;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;

public class V_2020121401_LetsEncrypt_Update_CA_on_new extends AbstractPluginUpgraderTask {

    @Override
    public void execute() {

        String caCertificate = ResourceTools.getResourceAsString("/com/foilen/infra/resource/letsencrypt/lets-encrypt-r3-cross-signed.pem");

        IPResourceService resourceService = commonServicesContext.getResourceService();

        ChangesContext changes = new ChangesContext(resourceService);

        List<WebsiteCertificate> certificatesToUpdate = resourceService.resourceFindAll( //
                resourceService.createResourceQuery(WebsiteCertificate.class) //
                        .addEditorEquals(LetsEncryptWebsiteCertificateEditor.EDITOR_NAME) //
                        .propertyLesserAndEquals(WebsiteCertificate.PROPERTY_START, DateTools.addDate(new Date(), Calendar.WEEK_OF_YEAR, 3) //
                        ));

        certificatesToUpdate.forEach(websiteCertificate -> {
            websiteCertificate.setCaCertificate(caCertificate);
            changes.resourceUpdate(websiteCertificate);
        });

        if (changes.hasChanges()) {
            internalServicesContext.getInternalChangeService().changesExecute(changes);
        }

    }

    @Override
    public String useTracker() {
        return UpgradeTask.DEFAULT_TRACKER;
    }

}
