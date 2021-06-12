/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import java.util.Objects;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.infra.resource.domain.Domain;
import com.google.common.collect.ComparisonChain;

/**
 * This is a DNS entry.<br>
 *
 * Links to:
 * <ul>
 * <li>None</li>
 * </ul>
 *
 * Manages:
 * <ul>
 * <li>{@link Domain}: (optional / many) MANAGES - The domains</li>
 * </ul>
 */
public class DnsEntry extends AbstractIPResource implements Comparable<DnsEntry> {

    public static final String RESOURCE_TYPE = "Dns Entry";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_DETAILS = "details";
    public static final String PROPERTY_PRIORITY = "priority";
    public static final String PROPERTY_WEIGHT = "weight";
    public static final String PROPERTY_PORT = "port";

    private String name;
    private DnsEntryType type = DnsEntryType.A;
    private String details;

    private int priority = 10;
    private int weight = 1;
    private int port = 0;

    public DnsEntry() {
    }

    /**
     * Primary key.
     *
     * @param name
     *            the name
     * @param type
     *            the type
     * @param details
     *            the details
     */
    public DnsEntry(String name, DnsEntryType type, String details) {
        this.name = name;
        this.type = type;
        this.details = details;
    }

    @Override
    public int compareTo(DnsEntry o) {
        ComparisonChain cc = ComparisonChain.start();
        cc = cc.compare(name, o.name);
        cc = cc.compare(type, o.type);
        cc = cc.compare(details, o.details);
        cc = cc.compare(priority, o.priority);
        cc = cc.compare(weight, o.weight);
        cc = cc.compare(port, o.port);
        return cc.result();
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() != getClass()) {
            return false;
        }

        DnsEntry se = (DnsEntry) o;
        return Objects.equals(name, se.name) //
                && Objects.equals(type, se.type) //
                && Objects.equals(details, se.details) //
                && Objects.equals(priority, se.priority) //
                && Objects.equals(weight, se.weight) //
                && Objects.equals(port, se.port);
    }

    public String getDetails() {
        return details;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.NET;
    }

    @Override
    public String getResourceDescription() {
        if (type == DnsEntryType.MX) {
            return details + " (" + priority + ")";
        }
        return details;
    }

    @Override
    public String getResourceName() {
        return name + " / " + type;
    }

    public DnsEntryType getType() {
        return type;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(details, name, type, priority, weight, port);
    }

    public DnsEntry setDetails(String details) {
        this.details = details;
        return this;
    }

    public DnsEntry setMxPriority(int mxPriority) {
        this.priority = mxPriority;
        return this;
    }

    public DnsEntry setName(String name) {
        this.name = name;
        return this;
    }

    public DnsEntry setPort(int port) {
        this.port = port;
        return this;
    }

    public DnsEntry setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public DnsEntry setType(DnsEntryType type) {
        this.type = type;
        return this;
    }

    public DnsEntry setWeight(int weight) {
        this.weight = weight;
        return this;
    }

}
