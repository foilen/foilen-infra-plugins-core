/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.email.resources;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.dns.DnsEntry;
import com.foilen.infra.resource.dns.DnsPointer;
import com.foilen.infra.resource.domain.Domain;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;

/**
 * This is a domain that is managed by an email server.<br/>
 * Links to:
 * <ul>
 * <li>{@link EmailServer}: (optional / 1) INSTALLED_ON - The email servers that manages that domain</li>
 * <li>{@link WebsiteCertificate}: (optional / 1) USES_SMTP - Needed for TLS (on domainName).</li>
 * <li>{@link WebsiteCertificate}: (optional / 1) USES_IMAP - Needed for TLS (on imapDomainName).</li>
 * <li>{@link WebsiteCertificate}: (optional / 1) USES_POP3 - Needed for TLS (on pop3DomainName).</li>
 * <li>{@link EmailRelay}: (optional / 1) SEND_THROUGHT - Send emails via the relay.</li>
 * </ul>
 *
 * Manages:
 * <ul>
 * <li>{@link Domain}: (optional / many) MANAGES - The domains</li>
 * <li>{@link DnsEntry}: Creates a {@link DnsEntry} for MX</li>
 * <li>{@link DnsPointer}: Creates a {@link DnsPointer} for As that point to the EmailServer's machines</li>
 * </ul>
 */
public class EmailDomain extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Email Domain";

    public static final String PROPERTY_DOMAIN_NAME = "domainName";
    public static final String PROPERTY_MX_DOMAIN_NAME = "mxDomainName";
    public static final String PROPERTY_IMAP_DOMAIN_NAME = "imapDomainName";
    public static final String PROPERTY_POP3_DOMAIN_NAME = "pop3DomainName";

    // Details
    private String domainName;
    private String mxDomainName;
    private String imapDomainName;
    private String pop3DomainName;

    public EmailDomain() {
    }

    /**
     * Primary key.
     *
     * @param domainName
     *            the domain name
     */
    public EmailDomain(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getImapDomainName() {
        return imapDomainName;
    }

    public String getMxDomainName() {
        return mxDomainName;
    }

    public String getPop3DomainName() {
        return pop3DomainName;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.EMAIL;
    }

    @Override
    public String getResourceDescription() {
        return "Email Domain";
    }

    @Override
    public String getResourceName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void setImapDomainName(String imapDomainName) {
        this.imapDomainName = imapDomainName;
    }

    public void setMxDomainName(String mxDomainName) {
        this.mxDomainName = mxDomainName;
    }

    public void setPop3DomainName(String pop3DomainName) {
        this.pop3DomainName = pop3DomainName;
    }

}
