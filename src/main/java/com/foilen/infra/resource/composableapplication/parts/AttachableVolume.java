/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.parts;

import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.AttachablePartContext;
import com.foilen.smalltools.tools.SecureRandomTools;

/**
 * To add a volume.
 */
public class AttachableVolume extends AttachablePart {

    public static final String RESOURCE_TYPE = "Attachable Volume";

    public static final String PROPERTY_UID = "uid";
    public static final String PROPERTY_HOST_FOLDER = "hostFolder";
    public static final String PROPERTY_CONTAINER_FOLDER = "containerFsFolder";

    // Details
    private String uid = SecureRandomTools.randomBase64String(10);
    private String hostFolder;
    private String containerFsFolder;

    public AttachableVolume() {
    }

    @Override
    public void attachTo(AttachablePartContext context) {
        context.getApplicationDefinition().addVolume(new IPApplicationDefinitionVolume(hostFolder, containerFsFolder));
    }

    public String getContainerFsFolder() {
        return containerFsFolder;
    }

    public String getHostFolder() {
        return hostFolder;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.INFRASTRUCTURE;
    }

    @Override
    public String getResourceDescription() {
        if (hostFolder == null) {
            return containerFsFolder;
        }
        return hostFolder + " -> " + containerFsFolder;
    }

    @Override
    public String getResourceName() {
        if (hostFolder == null) {
            return containerFsFolder;
        }
        return hostFolder + " -> " + containerFsFolder;
    }

    public String getUid() {
        return uid;
    }

    public void setContainerFsFolder(String containerFsFolder) {
        this.containerFsFolder = containerFsFolder;
    }

    public void setHostFolder(String hostFolder) {
        this.hostFolder = hostFolder;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
