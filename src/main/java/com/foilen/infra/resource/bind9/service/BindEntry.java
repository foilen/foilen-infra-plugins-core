/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9.service;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.collect.ComparisonChain;

/**
 * The low level entry.
 */
public class BindEntry extends AbstractBasics implements Comparable<BindEntry> {

    private String zone;
    private String subDomain;
    private DnsEntryType type;
    private String details;

    private int mxPriority = 10;

    public BindEntry() {
    }

    public BindEntry(String zone, String subDomain, DnsEntryType type, String details) {
        this.zone = zone;
        this.subDomain = subDomain;
        this.type = type;
        this.details = details;
    }

    @Override
    public int compareTo(BindEntry o) {
        ComparisonChain cc = ComparisonChain.start();
        cc = cc.compare(zone, o.zone);
        cc = cc.compare(subDomain, o.subDomain);
        cc = cc.compare(type, o.type);
        cc = cc.compare(details, o.details);
        cc = cc.compare(mxPriority, o.mxPriority);
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
        BindEntry be = (BindEntry) o;
        EqualsBuilder b = new EqualsBuilder();
        b.append(zone, be.zone);
        b.append(subDomain, be.subDomain);
        b.append(type, be.type);
        b.append(details, be.details);
        b.append(mxPriority, be.mxPriority);
        return b.isEquals();
    }

    public String getDetails() {
        return details;
    }

    public int getMxPriority() {
        return mxPriority;
    }

    public String getSubDomain() {
        return subDomain;
    }

    public DnsEntryType getType() {
        return type;
    }

    public String getZone() {
        return zone;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder b = new HashCodeBuilder();
        b.appendSuper(super.hashCode());
        b.append(zone);
        b.append(subDomain);
        b.append(type);
        b.append(details);
        b.append(mxPriority);
        return b.toHashCode();
    }

    public BindEntry setDetails(String details) {
        this.details = details;
        return this;
    }

    public BindEntry setMxPriority(int mxPriority) {
        this.mxPriority = mxPriority;
        return this;
    }

    public BindEntry setSubDomain(String subDomain) {
        this.subDomain = subDomain;
        return this;
    }

    public BindEntry setType(DnsEntryType type) {
        this.type = type;
        return this;
    }

    public BindEntry setZone(String zone) {
        this.zone = zone;
        return this;
    }

}
