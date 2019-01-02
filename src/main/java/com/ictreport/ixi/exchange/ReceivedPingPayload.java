package com.ictreport.ixi.exchange;

public class ReceivedPingPayload extends Payload {

    private final String uuid;
    private final PingPayload pingPayload;
    private final boolean isSenderDirectNeighbor;

    public ReceivedPingPayload(final String uuid, final PingPayload pingPayload, final boolean isSenderDirectNeighbor) {
        this.uuid = uuid;
        this.pingPayload = pingPayload;
        this.isSenderDirectNeighbor = isSenderDirectNeighbor;
    }

    public String getUuid() {
        return uuid;
    }

    public PingPayload getPingPayload() {
        return pingPayload;
    }

    public boolean isSenderDirectNeighbor() {
        return isSenderDirectNeighbor;
    }
}
