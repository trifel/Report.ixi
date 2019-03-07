package com.ictreport.ixi.exchange.payloads;

public class PingPayload extends Payload {

    private final String message;

    public PingPayload(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
