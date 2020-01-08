/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.webcertificate.helper;

import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.crypt.spongycastle.asymmetric.RSACrypt;
import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;

public class CertificateHelper {

    public static RSACertificate toRSACertificate(WebsiteCertificate websiteCertificate) {
        return RSACertificate.loadPemFromString( //
                websiteCertificate.getCertificate(), //
                websiteCertificate.getPrivateKey(), //
                websiteCertificate.getPublicKey() //
        );
    }

    public static WebsiteCertificate toWebsiteCertificate(String caCertificate, RSACertificate rsaCertificate) {
        WebsiteCertificate websiteCertificate = new WebsiteCertificate();
        toWebsiteCertificate(caCertificate, rsaCertificate, websiteCertificate);
        return websiteCertificate;
    }

    public static void toWebsiteCertificate(String caCertificate, RSACertificate rsaCertificate, WebsiteCertificate websiteCertificate) {
        websiteCertificate.setCaCertificate(caCertificate);
        websiteCertificate.setThumbprint(rsaCertificate.getThumbprint());
        websiteCertificate.setCertificate(rsaCertificate.saveCertificatePemAsString());
        websiteCertificate.setPublicKey(RSACrypt.RSA_CRYPT.savePublicKeyPemAsString(rsaCertificate.getKeysForSigning()));
        websiteCertificate.setPrivateKey(RSACrypt.RSA_CRYPT.savePrivateKeyPemAsString(rsaCertificate.getKeysForSigning()));
        websiteCertificate.setStart(rsaCertificate.getStartDate());
        websiteCertificate.setEnd(rsaCertificate.getEndDate());
        websiteCertificate.getDomainNames().addAll(rsaCertificate.getSubjectAltNames());
    }

}
