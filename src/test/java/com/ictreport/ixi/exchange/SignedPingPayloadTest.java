package com.ictreport.ixi.exchange;

import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.Cryptography;
import org.junit.Assert;
import org.junit.Test;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class SignedPingPayloadTest {

    @Test
    public void testSignedPingPayload() {
        final KeyPair keyPair = Cryptography.generateKeyPair(Constants.KEY_LENGTH);
        final PingPayload pingPayload = new PingPayload("abc");
        final SignedPayload signedPayload = new SignedPayload(pingPayload, keyPair.getPrivate());
        final String json = Payload.serialize(signedPayload);
        final Payload deserializedPayload = Payload.deserialize(json);

        // Determine what kind of payload it is.
        if (deserializedPayload instanceof SignedPayload) {
            final SignedPayload deserializedSignedPayload = (SignedPayload) deserializedPayload;

            Assert.assertNotNull(deserializedSignedPayload.getPayload());
            Assert.assertNotNull(deserializedSignedPayload.getSignature());
            Assert.assertTrue(deserializedSignedPayload.verify(keyPair.getPublic()));

            if (deserializedSignedPayload.getPayload() instanceof PingPayload) {
                PingPayload deserializedPingPayload = (PingPayload) deserializedSignedPayload.getPayload();

                Assert.assertEquals(deserializedPingPayload.getMessage(), pingPayload.getMessage());
            }
        } else {
            Assert.fail("Deserialization of polymorphism object failed.");
        }
    }
}
