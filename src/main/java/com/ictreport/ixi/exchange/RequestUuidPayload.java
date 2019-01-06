package com.ictreport.ixi.exchange;

public class RequestUuidPayload extends Payload {

    private final int port;

    public RequestUuidPayload(final int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}