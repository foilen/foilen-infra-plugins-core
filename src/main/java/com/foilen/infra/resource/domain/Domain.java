/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.domain;

import java.util.Objects;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.smalltools.tools.StringTools;

/**
 * This is a Domain. Mostly used for permissions over domain names.<br>
 *
 * Links to:
 * <ul>
 * <li>Domain: (optional / many) SUBDOMAIN - All the subdomains.</li>
 * </ul>
 */
public class Domain extends AbstractIPResource implements Comparable<Domain> {

    public static final String RESOURCE_TYPE = "Domain";

    public static final String PROPERTY_NAME = "name";

    public static final String LINK_TYPE_SUBDOMAIN = "SUBDOMAIN";

    // Details
    private String name;

    public Domain() {
    }

    /**
     * Primary key.
     *
     * @param name
     *            the name
     */
    public Domain(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Domain o) {
        return StringTools.safeComparisonNullFirst(name, o.name);
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

        Domain se = (Domain) o;
        return StringTools.safeEquals(name, se.name);
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
        return "";
    }

    @Override
    public String getResourceName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public void setName(String name) {
        this.name = name;
    }

}
