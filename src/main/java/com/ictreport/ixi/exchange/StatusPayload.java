package com.ictreport.ixi.exchange;

import java.util.List;

public class StatusPayload extends Payload {

    private final String uuid;
    private final String name;
    private final String reportIxiVersion;
    private final List<String> neighborUuids;

    public StatusPayload(final String uuid, final String name,
                         final String reportIxiVersion, final List<String> neighborUuids) {
        this.uuid = uuid;
        this.name = name;
        this.reportIxiVersion = reportIxiVersion;
        this.neighborUuids = neighborUuids;
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

    public List<String> getNeighborUuids() {
        return neighborUuids;
    }
}
