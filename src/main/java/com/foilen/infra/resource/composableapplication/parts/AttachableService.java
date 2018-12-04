/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.parts;

import java.util.List;

import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionService;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.AttachablePartContext;
import com.foilen.infra.resource.unixuser.UnixUser;

/**
 * This is a service to add to the application. <br/>
 * Links to:
 * <ul>
 * <li>{@link UnixUser}: (optional / 1) RUN_AS - The user that executes this services on the application</li>
 * </ul>
 */
public class AttachableService extends AttachablePart {

    public static final String RESOURCE_TYPE = "Attachable Service";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_COMMAND = "command";
    public static final String PROPERTY_WORKING_DIRECTORY = "workingDirectory";

    private String name;
    private String command;
    private String workingDirectory = null;

    public AttachableService() {
    }

    public AttachableService(String name, String command, String workingDirectory) {
        this.name = name;
        this.command = command;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public void attachTo(AttachablePartContext context) {

        // Command
        IPApplicationDefinitionService service = new IPApplicationDefinitionService(name, command);
        service.setWorkingDirectory(workingDirectory);

        // Unix user
        List<UnixUser> unixUsers = context.getServices().getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(this, LinkTypeConstants.RUN_AS, UnixUser.class);
        if (unixUsers.size() > 1) {
            throw new IllegalUpdateException("Must have a singe unix user to run as. Got " + unixUsers.size());
        }
        if (!unixUsers.isEmpty()) {
            service.setRunAs(unixUsers.get(0).getId());
        }

        context.getApplicationDefinition().getServices().add(service);
    }

    public String getCommand() {
        return command;
    }

    public String getName() {
        return name;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.INFRASTRUCTURE;
    }

    @Override
    public String getResourceDescription() {
        return "Service";
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

}
