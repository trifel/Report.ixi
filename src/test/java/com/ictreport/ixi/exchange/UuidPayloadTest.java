package com.ictreport.ixi.exchange;

import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.exchange.payloads.UuidPayload;
import org.junit.Assert;
import org.junit.Test;

public class UuidPayloadTest {

    @Test
    public void testUuidPayload() {
        final UuidPayload uuidPayload = new UuidPayload("abc");
        final String json = Payload.serialize(uuidPayload);
        final Payload deserializedPayload = Payload.deserialize(json);

        if (deserializedPayload instanceof UuidPayload) {
            final UuidPayload deserializedUuidPayload = (UuidPayload) deserializedPayload;
            Assert.assertEquals(deserializedUuidPayload.getUuid(), uuidPayload.getUuid());
        } else {
            Assert.fail("Deserialization of polymorphism object failed.");
        }
    }
}
