package com.ictreport.ixi.exchange;

import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.exchange.payloads.PingPayload;
import com.ictreport.ixi.exchange.payloads.ReceivedPingPayload;
import org.junit.Assert;
import org.junit.Test;

public class ReceivedPingPayloadTest {

    @Test
    public void testReceivedPingPayload() {
        final PingPayload pingPayload = new PingPayload("abc");
        final ReceivedPingPayload receivedPingPayload = new ReceivedPingPayload("abc", pingPayload);
        final String json = Payload.serialize(receivedPingPayload);
        final Payload deserializedPayload = Payload.deserialize(json);

        if (deserializedPayload instanceof ReceivedPingPayload) {
            final ReceivedPingPayload deserializedReceivedPingPayload = (ReceivedPingPayload) deserializedPayload;
            Assert.assertEquals(deserializedReceivedPingPayload.getUuid(), receivedPingPayload.getUuid());
            Assert.assertEquals(deserializedReceivedPingPayload.getPingPayload().getMessage(), pingPayload.getMessage());
        } else {
            Assert.fail("Deserialization of polymorphism object failed.");
        }
    }
}
