package com.ictreport.ixi.exchange.payloads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.net.DatagramPacket;

public class Payload {

    public static String serialize(final Payload payload) {
        final RuntimeTypeAdapterFactory<Payload> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
                .of(Payload.class, "type")
                .registerSubtype(PingPayload.class, "PingPayload")
                .registerSubtype(StatusPayload.class, "StatusPayload")
                .registerSubtype(ReceivedPingPayload.class, "ReceivedPingPayload")
                .registerSubtype(SubmittedPingPayload.class, "SubmittedPingPayload")
                .registerSubtype(RequestUuidPayload.class, "RequestUuidPayload")
                .registerSubtype(NeighborPayload.class, "NeighborPayload");

        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(runtimeTypeAdapterFactory).create();

        return gson.toJson(payload, Payload.class);
    }

    public static Payload deserialize(final String json) {
        final RuntimeTypeAdapterFactory<Payload> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
                .of(Payload.class, "type")
                .registerSubtype(PingPayload.class, "PingPayload")
                .registerSubtype(StatusPayload.class, "StatusPayload")
                .registerSubtype(ReceivedPingPayload.class, "ReceivedPingPayload")
                .registerSubtype(SubmittedPingPayload.class, "SubmittedPingPayload")
                .registerSubtype(RequestUuidPayload.class, "RequestUuidPayload")
                .registerSubtype(NeighborPayload.class, "NeighborPayload");

        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(runtimeTypeAdapterFactory).create();

        return gson.fromJson(json, Payload.class);
    }

    public static Payload deserialize(final DatagramPacket packet) {
        return Payload.deserialize(new String(packet.getData(), 0, packet.getLength()));
    }
}
