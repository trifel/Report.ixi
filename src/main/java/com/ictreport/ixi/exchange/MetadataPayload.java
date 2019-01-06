package com.ictreport.ixi.exchange;

import com.ictreport.ixi.utils.Cryptography;
import org.apache.commons.codec.binary.Base64;
import java.security.PublicKey;

public class MetadataPayload extends Payload {

    private final String uuid;
    private final String publicKey;
    private final String reportIxiVersion;

    public MetadataPayload(final String uuid, final PublicKey publicKey, final String reportIxiVersion) {
        this.uuid = uuid;
        this.publicKey = Base64.encodeBase64String(publicKey.getEncoded());
        this.reportIxiVersion = reportIxiVersion;
    }

    public String getUuid() {
        return uuid;
    }

    public PublicKey getPublicKey() {
        return Cryptography.getPublicKeyFromBytes(Base64.decodeBase64(publicKey.getBytes()));
    }

    public String getReportIxiVersion() {
        return reportIxiVersion;
    }
}
