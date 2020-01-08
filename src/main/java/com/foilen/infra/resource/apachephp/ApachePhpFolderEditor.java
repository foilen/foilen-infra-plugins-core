/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.smalltools.tools.DirectoryTools;
import com.google.common.base.Strings;

public class ApachePhpFolderEditor extends SimpleResourceEditor<ApachePhpFolder> {

    public static final String EDITOR_NAME = "Apache PHP Folder";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(ApachePhpFolder.PROPERTY_BASE_PATH, fieldConfig -> {
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
        simpleResourceEditorDefinition.addInputText(ApachePhpFolder.PROPERTY_RELATIVE_PATH, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(DirectoryTools::cleanupDots);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

        simpleResourceEditorDefinition.addInputText(ApachePhpFolder.PROPERTY_ALIAS, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(DirectoryTools::cleanupDots);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

    }

    @Override
    public Class<ApachePhpFolder> getForResourceType() {
        return ApachePhpFolder.class;
    }

}
