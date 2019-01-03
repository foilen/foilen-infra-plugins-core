/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9.service;

import java.util.List;

import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.resource.dns.DnsEntry;

/**
 * Create a bind file.
 */
public interface Bind9Service {

    /**
     * Create zone config files from the bind entries. It uses the default serial like 2015010100.
     *
     * @param dnsHostName
     *            the hostname of the serving dns
     * @param dnsAdminEmail
     *            the email address of the administrator
     * @param dnsConfigAssetsBundle
     *            the asset bundle where to store the configs
     * @param bindEntries
     *            all the sorted entries
     * @param containerConfigDir
     *            the config dir path in the container
     */
    void createBindFilesFromBindEntries(String dnsHostName, String dnsAdminEmail, IPApplicationDefinitionAssetsBundle dnsConfigAssetsBundle, List<BindEntry> bindEntries, String containerConfigDir);

    /**
     * Create zone config files from the bind entries.
     *
     * @param dnsHostName
     *            the hostname of the serving dns
     * @param dnsAdminEmail
     *            the email address of the administrator
     * @param serial
     *            the serial to use
     * @param dnsConfigAssetsBundle
     *            the asset bundle where to store the configs
     * @param bindEntries
     *            all the sorted entries
     * @param containerConfigDir
     *            the config dir path in the container
     */
    void createBindFilesFromBindEntries(String dnsHostName, String dnsAdminEmail, String serial, IPApplicationDefinitionAssetsBundle dnsConfigAssetsBundle, List<BindEntry> bindEntries,
            String containerConfigDir);

    /**
     * Create zone config files from the bind entries. It uses the default serial like 2015010100.
     *
     * @param dnsHostName
     *            the hostname of the serving dns
     * @param dnsAdminEmail
     *            the email address of the administrator
     * @param dnsConfigAssetsBundle
     *            the asset bundle where to store the configs
     * @param dnsEntries
     *            all the entries
     * @param containerConfigDir
     *            the config dir path in the container
     */
    void createBindFilesFromDnsEntries(String dnsHostName, String dnsAdminEmail, IPApplicationDefinitionAssetsBundle dnsConfigAssetsBundle, List<DnsEntry> dnsEntries, String containerConfigDir);

}
