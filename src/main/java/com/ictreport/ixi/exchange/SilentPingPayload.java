package com.ictreport.ixi.exchange;

public class SilentPingPayload extends Payload {

    private final String message;

    public SilentPingPayload(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
