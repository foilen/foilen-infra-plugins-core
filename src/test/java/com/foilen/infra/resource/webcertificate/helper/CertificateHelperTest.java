/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.webcertificate.helper;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.crypt.spongycastle.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.spongycastle.asymmetric.RSACrypt;
import com.foilen.smalltools.crypt.spongycastle.cert.CertificateDetails;
import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;
import com.foilen.smalltools.tools.DateTools;

public class CertificateHelperTest {

    @Test
    public void testWebsiteCertificate() {

        AsymmetricKeys keysForSigning = RSACrypt.RSA_CRYPT.generateKeyPair(1024);

        // One way
        RSACertificate rsaCertificate = new RSACertificate(keysForSigning);
        rsaCertificate.selfSign(new CertificateDetails() //
                .setCommonName("myName") //
                .setEndDate(DateTools.parseFull("2017-01-01 00:00:00")));

        WebsiteCertificate websiteCertificate = CertificateHelper.toWebsiteCertificate(null, rsaCertificate);
        Assert.assertNotNull(websiteCertificate);

        // Way back
        RSACertificate rsaCertificateBack = CertificateHelper.toRSACertificate(websiteCertificate);
        Assert.assertNotNull(rsaCertificateBack);

        Assert.assertEquals("myName", rsaCertificateBack.getCommonName());
        Assert.assertEquals(DateTools.parseFull("2017-01-01 00:00:00"), rsaCertificateBack.getEndDate());
    }

}
