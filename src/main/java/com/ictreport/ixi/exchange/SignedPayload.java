package com.ictreport.ixi.exchange;

import com.ictreport.ixi.utils.Cryptography;
import org.apache.commons.codec.binary.Base64;
import java.security.PrivateKey;
import java.security.PublicKey;

public class SignedPayload extends Payload {

    private final Payload payload;
    private final String signature;

    public SignedPayload(final Payload payload, final PrivateKey privateKey) {
        this.payload = payload;

        final String serializedPayload = Payload.serialize(payload);
        this.signature = Base64.encodeBase64String(Cryptography.sign(serializedPayload.getBytes(), privateKey));
    }

    public boolean verify(final PublicKey publicKey) {
        final String serializedPayload = Payload.serialize(payload);
        return Cryptography.verify(serializedPayload.getBytes(), Base64.decodeBase64(signature), publicKey);
    }

    public Payload getPayload() {
        return payload;
    }

    public String getSignature() {
        return signature;
    }
}
