package com.ictreport.ixi;

import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.exchange.processors.PingPayloadProcessor;
import com.ictreport.ixi.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.ReportIxi;
import org.iota.ict.model.transaction.Transaction;
import org.iota.ict.network.gossip.GossipEvent;
import org.iota.ict.network.gossip.GossipFilter;
import org.iota.ict.network.gossip.GossipListener;

public class ReportIxiGossipListener implements GossipListener {

    private static final Logger log = LogManager.getLogger("ReportIxiGossipListener");
    private final GossipFilter filter = new GossipFilter();
    private final ReportIxi reportIxi;
    private PingPayloadProcessor pingPayloadProcessor = new PingPayloadProcessor();

    public ReportIxiGossipListener(final ReportIxi reportIxi) {
        this.reportIxi = reportIxi;
        filter.watchTag(Constants.TAG);
    }

    public void onGossipEvent(final GossipEvent event) {
        if (!filter.passes(event.getTransaction())) return;

        if (event.isOwnTransaction()) {
            handleOutbound(event.getTransaction());
        } else {
            handleInbound(event.getTransaction());
        }
    }

    private void handleInbound(final Transaction transaction) {
        if (reportIxi != null) {
            try {
                final Payload payload = Payload.deserialize(transaction.decodedSignatureFragments());
                pingPayloadProcessor.process(reportIxi, null, payload);
            } catch (final Exception e) {
                // Invalid payload received, ignore it...
            }
        }
    }

    private void handleOutbound(final Transaction transaction) {

    }
}
