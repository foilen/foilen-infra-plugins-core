/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication.parts;

import java.util.SortedSet;
import java.util.TreeSet;

import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.AttachablePartContext;

/**
 * This is to install more applications.
 */
public class AttachableAptInstall extends AttachablePart {

    public static final String RESOURCE_TYPE = "Attachable Apt Install";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_PACKAGES = "packages";

    private String name;
    private SortedSet<String> packages = new TreeSet<>();

    public AttachableAptInstall() {
    }

    public AttachableAptInstall(String name, SortedSet<String> packages) {
        this.name = name;
        this.packages = packages;
    }

    @Override
    public void attachTo(AttachablePartContext context) {

        StringBuilder sb = new StringBuilder();
        sb.append("export TERM=dumb ; export DEBIAN_FRONTEND=noninteractive ; apt-get update && apt-get install -y");
        packages.forEach(p -> {
            sb.append(" ").append(p);
        });
        sb.append(" && apt-get clean && rm -rf /var/lib/apt/lists/*");

        context.getApplicationDefinition().addBuildStepCommand(sb.toString());

    }

    public String getName() {
        return name;
    }

    public SortedSet<String> getPackages() {
        return packages;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.INFRASTRUCTURE;
    }

    @Override
    public String getResourceDescription() {
        return "Apt Install";
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPackages(SortedSet<String> packages) {
        this.packages = packages;
    }

}
