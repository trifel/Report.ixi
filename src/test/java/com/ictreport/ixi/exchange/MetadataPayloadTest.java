package com.ictreport.ixi.exchange;

import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.Cryptography;
import org.junit.Assert;
import org.junit.Test;
import java.security.KeyPair;

public class MetadataPayloadTest {

    @Test
    public void testMetadataPayload() {
        final KeyPair keyPair = Cryptography.generateKeyPair(Constants.KEY_LENGTH);
        final MetadataPayload metadataPayload = new MetadataPayload("abc",
                keyPair.getPublic(), Constants.VERSION);
        final String json = Payload.serialize(metadataPayload);
        final Payload deserializedPayload = Payload.deserialize(json);

        if (deserializedPayload instanceof MetadataPayload) {
            final MetadataPayload deserializedMetadataPayload = (MetadataPayload) deserializedPayload;
            Assert.assertEquals(deserializedMetadataPayload.getUuid(), metadataPayload.getUuid());
            Assert.assertEquals(deserializedMetadataPayload.getPublicKey(), metadataPayload.getPublicKey());
        } else {
            Assert.fail("Deserialization of polymorphism object failed.");
        }
    }
}
