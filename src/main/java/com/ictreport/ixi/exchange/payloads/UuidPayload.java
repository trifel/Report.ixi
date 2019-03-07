package com.ictreport.ixi.exchange.payloads;

public class UuidPayload extends Payload {

    private final String uuid;

    public UuidPayload(final String uuid) {

        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

}
