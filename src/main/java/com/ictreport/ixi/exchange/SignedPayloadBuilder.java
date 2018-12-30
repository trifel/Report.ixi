package com.ictreport.ixi.exchange;

import com.google.gson.JsonObject;

import java.security.PrivateKey;

public class SignedPayloadBuilder {

    private IPayload payload = null;

    public SignedPayloadBuilder payload(IPayload payload) {

        this.payload = payload;
        return this;
    }

    public SignedPayload build(PrivateKey key) {

        SignedPayload signedPayload = new SignedPayload(payload, key);
        return signedPayload;
    }
}
