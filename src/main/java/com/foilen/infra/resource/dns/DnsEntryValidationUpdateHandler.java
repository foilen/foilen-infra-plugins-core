/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.dns;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.DomainValidator;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractUpdateEventHandler;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.smalltools.tuple.Tuple3;
import com.google.common.base.Strings;

public class DnsEntryValidationUpdateHandler extends AbstractUpdateEventHandler<DnsEntry> {

    private static Pattern startWithLetterValidationRegex = Pattern.compile("[a-zA-Z].*");
    private static List<DnsEntryType> typesWithDomain = Arrays.asList(DnsEntryType.A, DnsEntryType.AAAA, DnsEntryType.MX, DnsEntryType.NS);
    private static List<DnsEntryType> strictTypesWithDomain = Arrays.asList(DnsEntryType.A, DnsEntryType.AAAA, DnsEntryType.MX, DnsEntryType.NS);

    static protected void validate(CommonServicesContext services, DnsEntry dnsEntry) {
        if (!Strings.isNullOrEmpty(dnsEntry.getName())) {
            if (typesWithDomain.contains(dnsEntry.getType()) && !DomainValidator.getInstance().isValid(dnsEntry.getName())) {
                throw new IllegalUpdateException(services.getTranslationService().translate("error.notADomainName"));
            }
            if (strictTypesWithDomain.contains(dnsEntry.getType()) && !startWithLetterValidationRegex.matcher(dnsEntry.getName()).matches()) {
                throw new IllegalUpdateException(services.getTranslationService().translate("error.notStartingWithLetter"));
            }
            if (dnsEntry.getType() == DnsEntryType.A && !validIp(dnsEntry.getDetails())) {
                throw new IllegalUpdateException(services.getTranslationService().translate("error.notAnIp"));
            }
            if (dnsEntry.getName().contains(" ")) {
                throw new IllegalUpdateException(services.getTranslationService().translate("error.notADomainName"));
            }
        }
    }

    private static boolean validIp(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }
            for (int i = 0; i < 4; ++i) {
                int part = Integer.valueOf(parts[i]);
                if (part < 0 || part > 254) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void addHandler(CommonServicesContext services, ChangesContext changes, DnsEntry resource) {
        validate(services, resource);
    }

    @Override
    public void checkAndFix(CommonServicesContext services, ChangesContext changes, DnsEntry resource) {

    }

    @Override
    public void deleteHandler(CommonServicesContext services, ChangesContext changes, DnsEntry resource, List<Tuple3<IPResource, String, IPResource>> previousLinks) {
    }

    @Override
    public Class<DnsEntry> supportedClass() {
        return DnsEntry.class;
    }

    @Override
    public void updateHandler(CommonServicesContext services, ChangesContext changes, DnsEntry previousResource, DnsEntry newResource) {
        validate(services, newResource);
    }

}
