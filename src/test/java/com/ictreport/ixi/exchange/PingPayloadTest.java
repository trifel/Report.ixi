package com.ictreport.ixi.exchange;

import org.junit.Assert;
import org.junit.Test;

public class PingPayloadTest {

    @Test
    public void testPingPayload() {
        final PingPayload pingPayload = new PingPayload("abc");
        final String json = Payload.serialize(pingPayload);
        final Payload deserializedPayload = Payload.deserialize(json);

        if (deserializedPayload instanceof PingPayload) {
            final PingPayload deserializedPingPayload = (PingPayload) deserializedPayload;
            Assert.assertEquals(deserializedPingPayload.getMessage(), pingPayload.getMessage());
        } else {
            Assert.fail("Deserialization of polymorphism object failed.");
        }
    }
}
