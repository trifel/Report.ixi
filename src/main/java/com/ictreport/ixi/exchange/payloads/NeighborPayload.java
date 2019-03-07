package com.ictreport.ixi.exchange.payloads;

import com.google.gson.annotations.SerializedName;

public class NeighborPayload extends Payload {

    private final Long timestamp;
    private final String uuid;
    private final int all;
    @SerializedName("new")
    private final int newTx;
    private final int ignored;
    private final int invalid;
    private final int requested;

    public NeighborPayload(Long timestamp, String uuid, Integer all, Integer newTx, Integer ignored, Integer invalid, Integer requested) {
        this.timestamp = timestamp;
        this.uuid = (uuid != null ? uuid : "");
        this.all = (all != null ? all : -1);
        this.newTx = (newTx != null ? newTx : -1);
        this.ignored = (ignored != null ? ignored : -1);
        this.invalid = (invalid != null ? invalid : -1);
        this.requested = (requested != null ? requested : -1);
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getUuid() {
        return uuid;
    }

    public int getAll() {
        return all;
    }

    public int getNew() {
        return newTx;
    }

    public int getIgnored() {
        return ignored;
    }

    public int getInvalid() {
        return invalid;
    }

    public int getRequested() {
        return requested;
    }
}
