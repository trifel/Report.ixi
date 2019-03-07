package com.ictreport.ixi.exchange.processors;

import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.exchange.payloads.UuidPayload;
import com.ictreport.ixi.model.Neighbor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.ReportIxi;

public class UuidPayloadProcessor implements IPayloadProcessor {

    private static final Logger log = LogManager.getLogger("ReportIxi/UuidPayloadProcessor");

    public boolean process(final ReportIxi reportIxi, final Neighbor neighbor, final Payload payload) {

        if (!(payload instanceof UuidPayload)) {
            return false;
        }

        if (neighbor != null) {
            log.warn("Received a UuidPayload from a neighbor, this should not happen. Will not process...");
            return false;
        }

        UuidPayload uuidPayload = (UuidPayload)payload;

        log.debug(String.format(
                "Received UuidPayload from RCS: %s",
                Payload.serialize(uuidPayload))
        );

        reportIxi.getReportIxiContext().setUuid(uuidPayload.getUuid());
        log.info(String.format("Received uuid [%s] from RCS", reportIxi.getReportIxiContext().getUuid()));

        return true;
    }
}
