package com.ictreport.ixi.exchange;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

public class Payload {

    public static String serialize(final Payload payload) {

        RuntimeTypeAdapterFactory<Payload> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
                .of(Payload.class, "type")
                .registerSubtype(MetadataPayload.class, "MetadataPayload")
                .registerSubtype(PingPayload.class, "PingPayload")
                .registerSubtype(SignedPayload.class, "SignedPayload");

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(runtimeTypeAdapterFactory).create();

        final String json = gson.toJson(payload, Payload.class);

        return json;
    }

    public static Payload deserialize(final String json) {

        RuntimeTypeAdapterFactory<Payload> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
                .of(Payload.class, "type")
                .registerSubtype(MetadataPayload.class, "MetadataPayload")
                .registerSubtype(PingPayload.class, "PingPayload")
                .registerSubtype(SignedPayload.class, "SignedPayload");

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(runtimeTypeAdapterFactory).create();

        final Payload payload = gson.fromJson(json, Payload.class);

        return payload;
    }
}
