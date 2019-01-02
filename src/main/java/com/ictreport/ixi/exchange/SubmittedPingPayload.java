package com.ictreport.ixi.exchange;

public class SubmittedPingPayload extends Payload {

    private final String uuid;
    private final PingPayload pingPayload;

    public SubmittedPingPayload(final String uuid, final PingPayload pingPayload) {
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
