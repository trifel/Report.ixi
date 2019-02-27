package com.ictreport.ixi;

import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.exchange.Payload;
import com.ictreport.ixi.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.model.Transaction;
import org.iota.ict.network.event.GossipEvent;
import org.iota.ict.network.event.GossipFilter;
import org.iota.ict.network.event.GossipListener;

public class ReportIxiGossipListener implements GossipListener {

    private final static Logger LOGGER = LogManager.getLogger("ReportIxiGossipListener");
    private final GossipFilter filter = new GossipFilter();
    private final Api api;

    public ReportIxiGossipListener(final Api api) {
        this.api = api;
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
        if (api != null) {
            try {
                final Payload payload = Payload.deserialize(transaction.decodedSignatureFragments());
                api.getReceiver().processPayload(null, payload);
            } catch (final Exception e) {
                // Invalid payload received, ignore it...
            }
        }
    }

    private void handleOutbound(final Transaction transaction) {

    }
}
