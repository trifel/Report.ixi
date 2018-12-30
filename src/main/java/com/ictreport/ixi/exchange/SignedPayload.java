package com.ictreport.ixi.exchange;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ictreport.ixi.utils.Cryptography;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

public class SignedPayload implements IPayload {

    private final IPayload payload;
    private final byte[] signature;

    public SignedPayload(IPayload payload, PrivateKey key) {

        // Sign the payload
        final byte[] signature = Cryptography.sign(payload.toJsonBytes(), key);

        this.payload = payload;
        this.signature = signature;
    }

    @Override
    public JsonObject toJsonObject() {

        JsonObject signedPayload = new JsonObject();
        signedPayload.add("payload", payload.toJsonObject());

        return signedPayload;
    }

    @Override
    public String toJson() {

        Gson gson = new Gson();

        return gson.toJson(toJsonObject());
    }

    @Override
    public IPayload fromJson(String json) {

        Gson gson = new Gson();

        return gson.fromJson(json, UuidPayload.class);
    }

    @Override
    public byte[] toJsonBytes() {

        return toJson().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public IPayload fromJsonBytes(byte[] bytes) {

        return fromJson(new String(bytes));
    }

    @Override
    public String toJsonBytesBase64() {
        return Base64.encodeBase64String(toJsonBytes());
    }

    @Override
    public IPayload fromJsonBytesBase64(String jsonBytesBase64) {

        return fromJsonBytes(Base64.decodeBase64(jsonBytesBase64));
    }

    public IPayload getPayload() {

        return payload;
    }

    public byte[] getSignatureAsBytes() {

        return signature;
    }

    public String getSignatureAsBase64() {

        return Base64.encodeBase64String(getSignatureAsBytes());
    }
}
