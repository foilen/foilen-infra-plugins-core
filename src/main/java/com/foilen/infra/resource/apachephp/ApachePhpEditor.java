/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.tools.DirectoryTools;
import com.google.common.base.Strings;

public class ApachePhpEditor extends SimpleResourceEditor<ApachePhp> {

    public static final String EDITOR_NAME = "Apache PHP";

    public static String nullIfEmpty(String fieldValue) {
        if ("".equals(fieldValue)) {
            return null;
        }
        return fieldValue;
    }

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(ApachePhp.PROPERTY_NAME, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::toLowerCase);
            fieldConfig.addFormator(CommonFormatting::trimSpaces);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addValidator(CommonValidation::validateAlphaNumLower);
        });
        simpleResourceEditorDefinition.addInputText(ApachePhp.PROPERTY_VERSION, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(ApachePhp.PROPERTY_LOG_MAX_SIZE_M, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(ApachePhp.PROPERTY_MAX_UPLOAD_FILESIZE_M, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(CommonFormatting::nullIfEmpty);
            fieldConfig.setConvertFromString(v -> v == null ? 64 : Integer.valueOf(v));
        });
        simpleResourceEditorDefinition.addInputText(ApachePhp.PROPERTY_MAX_MEMORY_M, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(CommonFormatting::nullIfEmpty);
            fieldConfig.setConvertFromString(v -> v == null ? 64 * 3 : Integer.valueOf(v));
        });

        simpleResourceEditorDefinition.addInputText(ApachePhp.PROPERTY_DEFAULT_EMAIL_FROM, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(ApachePhpEditor::nullIfEmpty);
            fieldConfig.addValidator(CommonValidation::validateEmail);
            fieldConfig.setPopulateResource(ctx -> ctx.getEditedResourceBeanWrapper().setPropertyValue(ApachePhp.PROPERTY_DEFAULT_EMAIL_FROM, ctx.getTextValue()));
        });

        simpleResourceEditorDefinition.addInputText(ApachePhp.PROPERTY_BASE_PATH, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(DirectoryTools::cleanupDots);
            fieldConfig.addFormator(path -> {
                if (!Strings.isNullOrEmpty(path)) {
                    if (path.charAt(0) != '/') {
                        return "/" + path;
                    }
                }
                return path;
            });
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(ApachePhp.PROPERTY_MAIN_SITE_RELATIVE_PATH, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(DirectoryTools::cleanupDots);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

        simpleResourceEditorDefinition.addResource("unixUser", LinkTypeConstants.RUN_AS, UnixUser.class);
        simpleResourceEditorDefinition.addReverseResources("websitesFrom", Website.class, LinkTypeConstants.POINTS_TO);
        simpleResourceEditorDefinition.addResources("folders", LinkTypeConstants.USES, ApachePhpFolder.class);
        simpleResourceEditorDefinition.addResources("htPasswds", LinkTypeConstants.USES, ApachePhpHtPasswd.class);
        simpleResourceEditorDefinition.addResources("attachableParts", "ATTACHED", AttachablePart.class);
        simpleResourceEditorDefinition.addResources("machines", LinkTypeConstants.INSTALLED_ON, Machine.class);

    }

    @Override
    public Class<ApachePhp> getForResourceType() {
        return ApachePhp.class;
    }

}
