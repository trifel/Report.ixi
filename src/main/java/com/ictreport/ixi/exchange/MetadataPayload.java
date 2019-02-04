package com.ictreport.ixi.exchange;

public class MetadataPayload extends Payload {

    private final String uuid;
    private final String reportIxiVersion;

    public MetadataPayload(final String uuid, final String reportIxiVersion) {
        this.uuid = uuid;
        this.reportIxiVersion = reportIxiVersion;
    }

    public String getUuid() {
        return uuid;
    }

    public String getReportIxiVersion() {
        return reportIxiVersion;
    }
}
