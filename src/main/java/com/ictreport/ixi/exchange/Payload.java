package com.ictreport.ixi.exchange;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

public class Payload {

    public static String serialize(final Payload payload) {
        final RuntimeTypeAdapterFactory<Payload> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
                .of(Payload.class, "type")
                .registerSubtype(MetadataPayload.class, "MetadataPayload")
                .registerSubtype(PingPayload.class, "PingPayload")
                .registerSubtype(SilentPingPayload.class, "SilentPingPayload")
                .registerSubtype(SignedPayload.class, "SignedPayload")
                .registerSubtype(StatusPayload.class, "StatusPayload")
                .registerSubtype(ReceivedPingPayload.class, "ReceivedPingPayload")
                .registerSubtype(SubmittedPingPayload.class, "SubmittedPingPayload")
                .registerSubtype(RequestUuidPayload.class, "RequestUuidPayload")
                .registerSubtype(UuidPayload.class, "UuidPayload");

        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(runtimeTypeAdapterFactory).create();

        return gson.toJson(payload, Payload.class);
    }

    public static Payload deserialize(final String json) {
        final RuntimeTypeAdapterFactory<Payload> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
                .of(Payload.class, "type")
                .registerSubtype(MetadataPayload.class, "MetadataPayload")
                .registerSubtype(PingPayload.class, "PingPayload")
                .registerSubtype(SilentPingPayload.class, "SilentPingPayload")
                .registerSubtype(SignedPayload.class, "SignedPayload")
                .registerSubtype(StatusPayload.class, "StatusPayload")
                .registerSubtype(ReceivedPingPayload.class, "ReceivedPingPayload")
                .registerSubtype(SubmittedPingPayload.class, "SubmittedPingPayload")
                .registerSubtype(RequestUuidPayload.class, "RequestUuidPayload")
                .registerSubtype(UuidPayload.class, "UuidPayload");

        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(runtimeTypeAdapterFactory).create();

        return gson.fromJson(json, Payload.class);
    }
}
