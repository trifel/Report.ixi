package com.ictreport.ixi.exchange.processors;

import com.ictreport.ixi.exchange.payloads.MetadataPayload;
import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.model.Neighbor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.ReportIxi;

public class MetadataPayloadProcessor implements IPayloadProcessor {

    private static final Logger log = LogManager.getLogger("ReportIxi/MetadataPayloadProcessor");

    public boolean process(final ReportIxi reportIxi, final Neighbor neighbor, final Payload payload) {

        if (!(payload instanceof MetadataPayload)) {
            return false;
        }

        if (neighbor == null) {
            log.warn("Received a MetadataPayload from a unknown-neighbor. Will not process...");
            return false;
        }

        MetadataPayload metadataPayload = (MetadataPayload)payload;

        log.debug(String.format(
                "Received MetadataPayload from neighbor[%s]: %s",
                neighbor,
                Payload.serialize(metadataPayload))
        );

        if (neighbor.getReportIxiVersion() == null ||
                !neighbor.getReportIxiVersion().equals(metadataPayload.getReportIxiVersion())) {
            neighbor.setReportIxiVersion(metadataPayload.getReportIxiVersion());
            log.info(String.format("Neighbor[%s] operates Report.ixi version: %s",
                    neighbor.getAddress().getReportSocketAddress(),
                    neighbor.getReportIxiVersion()));
        }

        if (neighbor.getUuid() == null ||
                !neighbor.getUuid().equals(metadataPayload.getUuid())) {
            neighbor.setUuid(metadataPayload.getUuid());
            log.info(String.format("Received new uuid from neighbor[%s]",
                    neighbor.getAddress().getReportSocketAddress()));
        }

        return true;
    }
}
