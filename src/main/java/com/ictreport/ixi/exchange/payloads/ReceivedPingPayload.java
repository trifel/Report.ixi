package com.ictreport.ixi.exchange.payloads;

public class ReceivedPingPayload extends Payload {

    private final String uuid;
    private final PingPayload pingPayload;

    public ReceivedPingPayload(final String uuid, final PingPayload pingPayload) {
        this.uuid = uuid;
        this.pingPayload = pingPayload;
    }

    public String getUuid() {
        return uuid;
    }

    public PingPayload getPingPayload() {
        return pingPayload;
    }
}
