package com.ictreport.ixi.exchange;

import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.exchange.payloads.RequestUuidPayload;
import org.junit.Assert;
import org.junit.Test;

public class RequestUuidPayloadTest {

    @Test
    public void testRequestUuidPayload() {
        final RequestUuidPayload requestUuidPayload = new RequestUuidPayload("", -1);
        final String json = Payload.serialize(requestUuidPayload);
        final Payload deserializedPayload = Payload.deserialize(json);

        if (deserializedPayload instanceof RequestUuidPayload) {
            final RequestUuidPayload deserializedRequestUuidPayload = (RequestUuidPayload) deserializedPayload;
            Assert.assertEquals(deserializedRequestUuidPayload.getPort(), requestUuidPayload.getPort());
        } else {
            Assert.fail("Deserialization of polymorphism object failed.");
        }
    }
}
