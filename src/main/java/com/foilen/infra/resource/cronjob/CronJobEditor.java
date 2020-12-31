/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.cronjob;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;

public class CronJobEditor extends SimpleResourceEditor<CronJob> {

    public static final String EDITOR_NAME = "Cron Job";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(CronJob.PROPERTY_DESCRIPTION, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
        });
        simpleResourceEditorDefinition.addInputText(CronJob.PROPERTY_TIME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfigConsumer.addValidator(CommonValidation::validateCronTime);
        });
        simpleResourceEditorDefinition.addInputText(CronJob.PROPERTY_COMMAND, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfigConsumer.addValidator(CommonValidation::validatePath);
        });
        simpleResourceEditorDefinition.addInputText(CronJob.PROPERTY_WORKING_DIRECTORY, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfigConsumer.addValidator(CommonValidation::validatePath);
        });

        simpleResourceEditorDefinition.addResource("unixUser", LinkTypeConstants.RUN_AS, UnixUser.class);
        simpleResourceEditorDefinition.addResource("application", LinkTypeConstants.USES, Application.class);
        simpleResourceEditorDefinition.addResources("machines", LinkTypeConstants.INSTALLED_ON, Machine.class);

    }

    @Override
    public Class<CronJob> getForResourceType() {
        return CronJob.class;
    }

}
