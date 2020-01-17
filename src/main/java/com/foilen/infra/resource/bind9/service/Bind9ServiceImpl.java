/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9.service;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.resource.bind9.util.StoreInAssetBundleOnCloseOutputStream;
import com.foilen.infra.resource.dns.DnsEntry;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CloseableTools;

public class Bind9ServiceImpl extends AbstractBasics implements Bind9Service {

    private static final int TTL = 300;

    @Override
    public void createBindFilesFromBindEntries(String dnsHostName, String dnsAdminEmail, IPApplicationDefinitionAssetsBundle dnsConfigAssetsBundle, List<BindEntry> bindEntries,
            String containerConfigDir) {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
        String serial = sdf.format(new Date()) + "00";
        createBindFilesFromBindEntries(dnsHostName, dnsAdminEmail, serial, dnsConfigAssetsBundle, bindEntries, containerConfigDir);
    }

    @Override
    public void createBindFilesFromBindEntries(String dnsHostName, String dnsAdminEmail, String serial, IPApplicationDefinitionAssetsBundle dnsConfigAssetsBundle, List<BindEntry> bindEntries,
            String containerConfigDir) {
        String baseConfigPath = containerConfigDir;
        if (!baseConfigPath.endsWith("/") && !baseConfigPath.endsWith("\\")) {
            baseConfigPath += File.separatorChar;
        }

        // Create named.conf.local
        String namedFileName = baseConfigPath + "named.conf.local";

        OutputStream out = new StoreInAssetBundleOnCloseOutputStream(dnsConfigAssetsBundle, namedFileName);
        PrintWriter namedPW = new PrintWriter(out);

        // Create zone files
        if (!bindEntries.isEmpty()) {

            Collections.sort(bindEntries);

            // New file
            String currentZone = bindEntries.get(0).getZone();
            PrintWriter zonePW = openNextZone(namedPW, null, dnsConfigAssetsBundle, baseConfigPath, currentZone);
            writeHeader(zonePW, dnsHostName, dnsAdminEmail, TTL, serial, currentZone);

            // Go through all entries
            for (BindEntry bindEntry : bindEntries) {
                // Check if the same zone
                if (!bindEntry.getZone().equals(currentZone)) {
                    // New file
                    currentZone = bindEntry.getZone();
                    zonePW = openNextZone(namedPW, zonePW, dnsConfigAssetsBundle, baseConfigPath, currentZone);
                    writeHeader(zonePW, dnsHostName, dnsAdminEmail, TTL, serial, currentZone);
                }

                // Add the line
                switch (bindEntry.getType()) {
                case CNAME:
                case NS:
                    zonePW.println(bindEntry.getSubDomain() + " " + TTL + " " + bindEntry.getType() + " " + bindEntry.getDetails() + ".");
                    break;
                case MX:
                    zonePW.println(bindEntry.getSubDomain() + " " + TTL + " " + bindEntry.getType() + " " + bindEntry.getMxPriority() + " " + bindEntry.getDetails() + ".");
                    break;
                case TXT:
                    zonePW.println(bindEntry.getSubDomain() + " " + TTL + " " + bindEntry.getType() + " \"" + bindEntry.getDetails() + "\"");
                    break;
                default:
                    zonePW.println(bindEntry.getSubDomain() + " " + TTL + " " + bindEntry.getType() + " " + bindEntry.getDetails());
                    break;

                }
            }

            CloseableTools.close(zonePW);
        }

        CloseableTools.close(namedPW);
    }

    @Override
    public void createBindFilesFromDnsEntries(String dnsHostName, String dnsAdminEmail, IPApplicationDefinitionAssetsBundle dnsConfigAssetsBundle, List<DnsEntry> dnsEntries,
            String containerConfigDir) {

        List<BindEntry> bindEntries = dnsEntries.stream() //
                .map(entry -> {
                    String zone = getZoneFromDomain(entry.getName());
                    String subDomain = getSubDomainFromDomain(entry.getName());
                    return new BindEntry(zone, subDomain, entry.getType(), entry.getDetails()).setMxPriority(entry.getMxPriority());
                }) //
                .collect(Collectors.toList());

        logger.debug("BindEntries ; amount {}", bindEntries.size());
        bindEntries.forEach(it -> {
            logger.debug("\t{}", it);
        });

        createBindFilesFromBindEntries(dnsHostName, dnsAdminEmail, dnsConfigAssetsBundle, bindEntries, containerConfigDir);
    }

    private int getIndexOfDemarcation(String domain) {
        int pos = domain.length();
        for (int i = 0; i < 2; ++i) {
            pos = domain.lastIndexOf('.', pos - 1);
            if (pos == -1) {
                return 0;
            }
        }

        return pos;
    }

    private String getSubDomainFromDomain(String domain) {
        int index = getIndexOfDemarcation(domain);
        if (index == 0) {
            return domain + ".";
        } else {
            return domain.substring(0, index);
        }
    }

    private String getZoneFromDomain(String domain) {
        int index = getIndexOfDemarcation(domain);
        if (index == 0) {
            return domain;
        } else {
            return domain.substring(index + 1);
        }
    }

    private PrintWriter openNextZone(PrintWriter namedPW, PrintWriter oldPW, IPApplicationDefinitionAssetsBundle dnsConfigAssetsBundle, String baseConfigPath, String newZoneName) {
        // Close previous
        CloseableTools.close(oldPW);

        // Put new in named file
        String zoneFilePath = "pri." + newZoneName;
        namedPW.println("zone \"" + newZoneName + "\" {");
        namedPW.println("  type master;");
        namedPW.println("  allow-transfer {none;};");
        namedPW.println("  file \"/etc/bind/" + zoneFilePath + "\";");
        namedPW.println("};");

        // Create new
        OutputStream out = new StoreInAssetBundleOnCloseOutputStream(dnsConfigAssetsBundle, baseConfigPath + zoneFilePath);
        return new PrintWriter(out);
    }

    private void writeHeader(PrintWriter zonePW, String dnsHostName, String dnsAdminEmail, int ttl, String serial, String zone) {
        dnsAdminEmail = dnsAdminEmail.replace('@', '.');
        zonePW.println("$TTL " + ttl);
        zonePW.println("@ IN SOA " + dnsHostName + ". " + dnsAdminEmail + ". (");
        zonePW.println("  " + serial + "; serial number");
        zonePW.println("  3600; refresh [1h]");
        zonePW.println("  600; retry [10m]");
        zonePW.println("  86400; expire [1d]");
        zonePW.println("  " + ttl + "); min TTL");
        zonePW.println(";");
        zonePW.println();
        zonePW.println(zone + ". " + TTL + " " + DnsEntryType.NS + " " + dnsHostName + ".");
    }

}
