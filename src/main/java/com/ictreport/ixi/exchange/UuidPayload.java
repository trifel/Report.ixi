package com.ictreport.ixi.exchange;

public class UuidPayload extends Payload {

    public final String uuid;

    public UuidPayload(final String uuid) {

        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

}
