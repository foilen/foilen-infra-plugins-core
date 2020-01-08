/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.parts;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.smalltools.tools.DirectoryTools;
import com.google.common.base.Strings;

public class AttachableVolumeEditor extends SimpleResourceEditor<AttachableVolume> {

    public static final String EDITOR_NAME = "Attachable Volume Folder";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(AttachableVolume.PROPERTY_HOST_FOLDER, fieldConfig -> {
            fieldConfig.addFormator(value -> {
                if (Strings.isNullOrEmpty(value)) {
                    return null;
                }
                value = CommonFormatting.trimSpacesAround(value);
                value = DirectoryTools.cleanupDots(value);
                return value;
            });
            fieldConfig.setConvertFromString(value -> value == null ? null : value);
        });

        simpleResourceEditorDefinition.addInputText(AttachableVolume.PROPERTY_CONTAINER_FOLDER, fieldConfig -> {
            fieldConfig.addFormator(value -> {
                if (Strings.isNullOrEmpty(value)) {
                    return null;
                }
                value = CommonFormatting.trimSpacesAround(value);
                value = DirectoryTools.cleanupDots(value);
                return value;
            });
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.setConvertFromString(value -> value == null ? null : value);
        });

    }

    @Override
    public Class<AttachableVolume> getForResourceType() {
        return AttachableVolume.class;
    }

}
