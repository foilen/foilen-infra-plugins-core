/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.google.common.collect.ComparisonChain;

/**
 * This is for having A and AAAA entries pointing to machines. <br>
 * Links to:
 * <ul>
 * <li>Machine: (optional / many) POINTS_TO - All the machines to point to.</li>
 * </ul>
 *
 * Manages:
 * <ul>
 * <li>{@link DnsEntry}: The A entries with the ips of the machines</li>
 * </ul>
 */
public class DnsPointer extends AbstractIPResource implements Comparable<DnsPointer> {

    public static final String RESOURCE_TYPE = "Dns Pointer";

    public static final String PROPERTY_NAME = "name";

    private String name;

    public DnsPointer() {
    }

    /**
     * Primary key.
     *
     * @param name
     *            the domaine name
     */
    public DnsPointer(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(DnsPointer o) {
        ComparisonChain cc = ComparisonChain.start();
        cc = cc.compare(name, o.name);
        return cc.result();
    }

    public String getName() {
        return name;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.NET;
    }

    @Override
    public String getResourceDescription() {
        return name;
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
