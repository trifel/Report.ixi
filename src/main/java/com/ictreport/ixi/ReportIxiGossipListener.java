package com.ictreport.ixi;

import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.exchange.Payload;
import com.ictreport.ixi.exchange.SignedPayload;

import org.iota.ict.model.Transaction;
import org.iota.ict.network.event.GossipEvent;
import org.iota.ict.network.event.GossipFilter;
import org.iota.ict.network.event.GossipListener;

public class ReportIxiGossipListener extends GossipListener {

    private final GossipFilter filter = new GossipFilter();
    private final Api api;

    public ReportIxiGossipListener(final Api api) {
        this.api = api;
        filter.watchTag("REPORT9IXI99999999999999999");
    }

    @Override
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
                final Payload payload = Payload.deserialize(transaction.decodedSignatureFragments);
                if (payload instanceof SignedPayload) {
                    api.getReceiver().processSignedPayload((SignedPayload)payload);
                }                
            } catch (final Exception e) {
                
            }
        }
    }

    private void handleOutbound(final Transaction transaction) {

    }
}
