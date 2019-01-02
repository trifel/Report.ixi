package com.ictreport.ixi.exchange;

import java.util.List;

public class StatusPayload extends Payload {

    public final String uuid;
    public final String name;
    public final String reportIxiVersion;
    public List<String> neighborUuids;

    public StatusPayload(final String uuid, final String name, final String reportIxiVersion, final List<String> neighborUuids) {
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
