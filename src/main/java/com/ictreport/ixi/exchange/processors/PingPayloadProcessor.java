package com.ictreport.ixi.exchange.processors;

import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.exchange.payloads.PingPayload;
import com.ictreport.ixi.exchange.payloads.ReceivedPingPayload;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.RCSRestCaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.ReportIxi;

public class PingPayloadProcessor implements IPayloadProcessor {

    private static final Logger log = LogManager.getLogger("ReportIxi/PingPayloadProcessor");

    public boolean process(final ReportIxi reportIxi, final Neighbor neighbor, final Payload payload) {

        if (!(payload instanceof PingPayload)) {
            return false;
        }

        if (neighbor != null) {
            return false;
        }

        PingPayload pingPayload = (PingPayload)payload;

        log.trace(String.format(
                "Received PingPayload from neighbor[%s]: %s",
                neighbor,
                Payload.serialize(pingPayload))
        );

        final ReceivedPingPayload receivedPingPayload =
                new ReceivedPingPayload(reportIxi.getReportIxiContext().getUuid(), pingPayload);

        if (reportIxi.getReportIxiContext().getUuid() != null) {
            RCSRestCaller.send("receivedPingPayload", receivedPingPayload);
        }

        return true;
    }
}
