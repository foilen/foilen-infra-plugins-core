/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.parts;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.unixuser.UnixUser;

public class AttachableContainerUserToChangeIdEditor extends SimpleResourceEditor<AttachableContainerUserToChangeId> {

    public static final String EDITOR_NAME = "Attachable Container User To Change Id";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(AttachableContainerUserToChangeId.PROPERTY_USERNAME_IN_CONTAINER, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpaces);
            fieldConfig.addValidator(CommonValidation::validateAlphaNumLowerAndUpper);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

        simpleResourceEditorDefinition.addResource("userToChangeIdTo", LinkTypeConstants.USES, UnixUser.class);

    }

    @Override
    public Class<AttachableContainerUserToChangeId> getForResourceType() {
        return AttachableContainerUserToChangeId.class;
    }

}
