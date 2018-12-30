package com.ictreport.ixi.exchange;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

public class UuidPayload implements IPayload {

    private String uuid;

    public UuidPayload() {

    }

    public UuidPayload(final String uuid) {

        this.uuid = uuid;
    }

    @Override
    public JsonObject toJsonObject() {

        JsonObject payload = new JsonObject();
        payload.addProperty("uuid", uuid);

        return payload;
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

    public String getUuid() {

        return uuid;
    }
}
