/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.infra.resource.test.AbstractCorePluginTest;

public class DnsEntryValidationChangesEventHandlerTest extends AbstractCorePluginTest {

    private void assertFail(List<String> results, DnsEntry dnsEntry) {
        try {
            DnsEntryValidationChangesEventHandler.validate(getCommonServicesContext(), dnsEntry);
            results.add("->[FAIL/OK] " + dnsEntry);
        } catch (IllegalUpdateException e) {
            results.add("[FAIL/FAIL] " + dnsEntry);
        }
    }

    private void assertOk(List<String> results, DnsEntry dnsEntry) {
        try {
            DnsEntryValidationChangesEventHandler.validate(getCommonServicesContext(), dnsEntry);
            results.add("[OK/OK] " + dnsEntry);
        } catch (IllegalUpdateException e) {
            results.add("->[OK/FAIL] " + dnsEntry);
        }
    }

    @Test
    public void testValidate() {

        List<String> results = new ArrayList<>();

        assertOk(results, new DnsEntry("example.com", DnsEntryType.NS, "ns1.example.com"));

        assertOk(results, new DnsEntry("www.example.com", DnsEntryType.A, "127.0.0.1"));
        assertOk(results, new DnsEntry("www.example-example.com", DnsEntryType.A, "127.0.0.1"));
        assertFail(results, new DnsEntry("www.example.com", DnsEntryType.A, "520.0.0.1"));
        assertFail(results, new DnsEntry("db_1.example.com", DnsEntryType.A, "127.0.0.1"));
        assertFail(results, new DnsEntry("db 1.example.com", DnsEntryType.A, "127.0.0.1"));
        assertFail(results, new DnsEntry("www-.example.com", DnsEntryType.A, "127.0.0.1"));
        assertFail(results, new DnsEntry("www.example.com", DnsEntryType.A, "a.b.c.d"));
        assertFail(results, new DnsEntry("10www.example.com", DnsEntryType.A, "127.0.0.1"));

        assertOk(results, new DnsEntry("www.example-example.com", DnsEntryType.CNAME, "www.example.com"));
        assertOk(results, new DnsEntry("8fifehwjkgrewg._domainkey.example.com", DnsEntryType.CNAME, "8fifehwjkgrewg.dkim.amazonses.com"));
        assertOk(results, new DnsEntry("db_1.example.com", DnsEntryType.CNAME, "www.example.com"));
        assertOk(results, new DnsEntry("10www.example.com", DnsEntryType.CNAME, "www.example.com"));
        assertOk(results, new DnsEntry("www-.example.com", DnsEntryType.CNAME, "www.example.com"));
        assertFail(results, new DnsEntry("db 1.example.com", DnsEntryType.CNAME, "www.example.com"));

        assertOk(results, new DnsEntry("_kerberos.example.com", DnsEntryType.TXT, "EXAMPLE.COM"));
        assertOk(results, new DnsEntry("_amazonses.example.com", DnsEntryType.TXT, "Kfujgkreuk+Kfiewkjhklgrew="));
        assertOk(results, new DnsEntry("example.com", DnsEntryType.TXT, "v=spf1 include:spf.mandrillapp.com include:amazonses.com include:sendgrid.net a mx ~all"));

        assertOk(results, new DnsEntry("example.com", DnsEntryType.MX, "mail.example.com"));
        assertOk(results, new DnsEntry("www.example-example.com", DnsEntryType.MX, "mail.example.com"));
        assertFail(results, new DnsEntry("db_1.example.com", DnsEntryType.MX, "mail.example.com"));
        assertFail(results, new DnsEntry("db 1.example.com", DnsEntryType.MX, "mail.example.com"));
        assertFail(results, new DnsEntry("www-.example.com", DnsEntryType.MX, "mail.example.com"));
        assertFail(results, new DnsEntry("10www.example.com", DnsEntryType.MX, "mail.example.com"));

        StringBuilder resultSb = new StringBuilder();
        results.forEach(it -> resultSb.append(it).append("\n"));
        String result = resultSb.toString();
        boolean hasFailures = result.contains("[FAIL/OK]") || result.contains("[OK/FAIL]");
        Assert.assertFalse(result, hasFailures);
    }

}
