/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9.service;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.google.common.collect.ComparisonChain;

/**
 * The low level entry.
 */
public class BindEntry implements Comparable<BindEntry> {

    private String zone;
    private String subDomain;
    private DnsEntryType type;
    private String details;

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
        return b.isEquals();
    }

    public String getDetails() {
        return details;
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
        return b.toHashCode();
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public void setType(DnsEntryType type) {
        this.type = type;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        return "BindEntry [zone=" + zone + ", subDomain=" + subDomain + ", type=" + type + ", details=" + details + "]";
    }

}
