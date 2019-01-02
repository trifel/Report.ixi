package com.ictreport.ixi.exchange;

import com.ictreport.ixi.utils.Cryptography;
import org.apache.commons.codec.binary.Base64;

import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class MetadataPayload extends Payload {

    public final String uuid;
    public final String publicKey;
    public final String reportIxiVersion;

    public MetadataPayload(final String uuid, final PublicKey publicKey, final String reportIxiVersion) {

        this.uuid = uuid;
        this.publicKey = Base64.encodeBase64String(publicKey.getEncoded());
        this.reportIxiVersion = reportIxiVersion;
    }

    public String getUuid() {
        return uuid;
    }

    public PublicKey getPublicKey() {
        try {
            return Cryptography.getPublicKeyFromBytes(Base64.decodeBase64(publicKey.getBytes()));
        } catch (InvalidKeySpecException e) {
            return null;
        }
    }

    public String getReportIxiVersion() {
        return reportIxiVersion;
    }
}
