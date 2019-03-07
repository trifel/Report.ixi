package com.ictreport.ixi.exchange;

import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.exchange.payloads.PingPayload;
import com.ictreport.ixi.exchange.payloads.SubmittedPingPayload;
import org.junit.Assert;
import org.junit.Test;

public class SubmittedPingPayloadTest {

    @Test
    public void testPingPayload() {
        final PingPayload pingPayload = new PingPayload("abc");
        final SubmittedPingPayload submittedPingPayload = new SubmittedPingPayload("abc", pingPayload);
        final String json = Payload.serialize(submittedPingPayload);
        final Payload deserializedPayload = Payload.deserialize(json);

        if (deserializedPayload instanceof SubmittedPingPayload) {
            final SubmittedPingPayload deserializedSubmittedPingPayload = (SubmittedPingPayload) deserializedPayload;
            Assert.assertEquals(deserializedSubmittedPingPayload.getUuid(), submittedPingPayload.getUuid());
            Assert.assertEquals(deserializedSubmittedPingPayload.getPingPayload().getMessage(), pingPayload.getMessage());
        } else {
            Assert.fail("Deserialization of polymorphism object failed.");
        }
    }
}
