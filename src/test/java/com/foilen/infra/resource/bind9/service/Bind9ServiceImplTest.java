/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.ResourceTools;
import com.foilen.smalltools.tuple.Tuple2;

public class Bind9ServiceImplTest {

    private Bind9ServiceImpl bind9Service = new Bind9ServiceImpl();

    private void assertStringWithUnixEol(String expected, String actual) {
        expected = expected.replace("\r", "");
        actual = actual.replace("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCreateBindFiles() {

        // Load
        List<BindEntry> bindEntries = JsonTools.readFromResourceAsList("Bind9ServiceImplTest-testCreateBindFiles-entries.json", BindEntry.class, getClass());

        // Execute
        IPApplicationDefinitionAssetsBundle dnsConfigAssetsBundle = new IPApplicationDefinitionAssetsBundle();
        bind9Service.createBindFilesFromBindEntries("ns1.example.org", "admin@example.com", "2015022500", dnsConfigAssetsBundle, bindEntries, "config/");

        List<Tuple2<String, String>> all = dnsConfigAssetsBundle.getAssetsRelativePathAndTextContent();
        Assert.assertEquals(3, all.size());

        // Check named.conf.local
        String expected = ResourceTools.getResourceAsString("Bind9ServiceImplTest-testCreateBindFiles-named.conf.local.txt", this.getClass());
        String actual = all.stream().filter(it -> it.getA().equals("config/named.conf.local")).findFirst().get().getB();
        assertStringWithUnixEol(expected, actual);

        // Check pri.example.com
        expected = ResourceTools.getResourceAsString("Bind9ServiceImplTest-testCreateBindFiles-pri.example.com.txt", this.getClass());
        actual = all.stream().filter(it -> it.getA().equals("config/pri.example.com")).findFirst().get().getB();
        assertStringWithUnixEol(expected, actual);

        // Check pri.example.org
        expected = ResourceTools.getResourceAsString("Bind9ServiceImplTest-testCreateBindFiles-pri.example.org.txt", this.getClass());
        actual = all.stream().filter(it -> it.getA().equals("config/pri.example.org")).findFirst().get().getB();
        assertStringWithUnixEol(expected, actual);
    }
}
