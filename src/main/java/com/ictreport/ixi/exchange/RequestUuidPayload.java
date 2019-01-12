package com.ictreport.ixi.exchange;

public class RequestUuidPayload extends Payload {

    private final int port;
    private final String uuid;

    public RequestUuidPayload(final String uuid, final int port) {
        this.uuid = uuid;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getUuid() {
        return uuid;
    }
}