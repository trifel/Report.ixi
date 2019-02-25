package com.ictreport.ixi.exchange;

import java.util.List;

public class StatusPayload extends Payload {

    private final String uuid;
    private final String name;
    private final String ictVersion;
    private final String reportIxiVersion;
    private final int    ictRoundDuration;
    private final List<NeighborPayload> neighbors;

    public StatusPayload(final String uuid, final String name, final String ictVersion,
                         final String reportIxiVersion, final int ictRoundDuration,
                         final List<NeighborPayload> neighbors) {
        this.uuid = uuid;
        this.name = name;
        this.ictVersion = ictVersion;
        this.reportIxiVersion = reportIxiVersion;
        this.ictRoundDuration = ictRoundDuration;
        this.neighbors = neighbors;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getIctVersion() {
        return ictVersion;
    }

    public String getReportIxiVersion() {
        return reportIxiVersion;
    }

    public int getIctRoundDuration() {
        return ictRoundDuration;
    }

    public List<NeighborPayload> getNeighbors() {
        return neighbors;
    }
}
