/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.parts;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.mariadb.MariaDBServer;

public class AttachableMariaDBEditor extends SimpleResourceEditor<AttachableMariaDB> {

    public static final String EDITOR_NAME = "Attachable MariaDB";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(AttachableMariaDB.PROPERTY_NAME, fieldConfigConsumer -> {
            fieldConfigConsumer.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });
        simpleResourceEditorDefinition.addInputText(AttachableMariaDB.PROPERTY_LOCAL_PORT, fieldConfigConsumer -> {
            fieldConfigConsumer.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

        simpleResourceEditorDefinition.addResource("mariaDbServer", LinkTypeConstants.POINTS_TO, MariaDBServer.class);

    }

    @Override
    public Class<AttachableMariaDB> getForResourceType() {
        return AttachableMariaDB.class;
    }

}
