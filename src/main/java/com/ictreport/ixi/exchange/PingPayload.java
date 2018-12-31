package com.ictreport.ixi.exchange;

public class PingPayload extends Payload {

    private final String uuid;

    public PingPayload(final String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
