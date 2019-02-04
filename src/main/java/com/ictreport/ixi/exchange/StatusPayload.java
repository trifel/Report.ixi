package com.ictreport.ixi.exchange;

import java.util.List;

public class StatusPayload extends Payload {

    private final String uuid;
    private final String name;
    private final String reportIxiVersion;
    private final List<NeighborPayload> neighbors;

    public StatusPayload(final String uuid, final String name,
                         final String reportIxiVersion, final List<NeighborPayload> neighbors) {
        this.uuid = uuid;
        this.name = name;
        this.reportIxiVersion = reportIxiVersion;
        this.neighbors = neighbors;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getReportIxiVersion() {
        return reportIxiVersion;
    }

    public List<NeighborPayload> getNeighbors() {
        return neighbors;
    }
}
