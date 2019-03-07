package com.ictreport.ixi.exchange;

import com.ictreport.ixi.exchange.payloads.MetadataPayload;
import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.utils.Constants;
import org.junit.Assert;
import org.junit.Test;

public class MetadataPayloadTest {

    @Test
    public void testMetadataPayload() {
        final MetadataPayload metadataPayload = new MetadataPayload("abc", Constants.VERSION);
        final String json = Payload.serialize(metadataPayload);
        final Payload deserializedPayload = Payload.deserialize(json);

        if (deserializedPayload instanceof MetadataPayload) {
            final MetadataPayload deserializedMetadataPayload = (MetadataPayload) deserializedPayload;
            Assert.assertEquals(deserializedMetadataPayload.getUuid(), metadataPayload.getUuid());
        } else {
            Assert.fail("Deserialization of polymorphism object failed.");
        }
    }
}
