package com.ictreport.ixi.exchange;

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

    public NeighborPayload(Long timestamp, String uuid, int all, int newTx, int ignored, int invalid, int requested) {
        this.timestamp = timestamp;
        this.uuid = (uuid != null ? uuid : "");
        this.all = all;
        this.newTx = newTx;
        this.ignored = ignored;
        this.invalid = invalid;
        this.requested = requested;
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
