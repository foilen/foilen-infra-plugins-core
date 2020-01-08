/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.global;

import java.util.Set;
import java.util.TreeSet;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.google.common.collect.ComparisonChain;

/**
 * This is for keeping track of changes.<br>
 */
public class UpgraderItem extends AbstractIPResource implements Comparable<UpgraderItem> {

    public static final String RESOURCE_TYPE = "Upgrader Item";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_APPLIED = "applied";

    private String name;
    private Set<String> applied = new TreeSet<>();

    public UpgraderItem() {
    }

    /**
     * Primary key.
     *
     * @param name
     *            the name
     */
    public UpgraderItem(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(UpgraderItem o) {
        return ComparisonChain.start() //
                .compare(this.name, o.name) //
                .result();
    }

    public Set<String> getApplied() {
        return applied;
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
        return "Upgrader for " + name;
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public void setApplied(Set<String> applied) {
        this.applied = applied;
    }

    public void setName(String name) {
        this.name = name;
    }

}
