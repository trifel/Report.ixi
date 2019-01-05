package com.ictreport.ixi.exchange;

import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.Cryptography;
import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class PayloadTest {

    @Test
    public void testMetadataPayload() {

        try {

            final KeyPair keyPair = Cryptography.generateKeyPair(1024);

            MetadataPayload metadataPayload = new MetadataPayload("abc", keyPair.getPublic(), Constants.VERSION);

            final String json = Payload.serialize(metadataPayload);

            final Payload deserializedPayload = Payload.deserialize(json);

            if (deserializedPayload instanceof MetadataPayload) {
                MetadataPayload deserializedMetadataPayload = (MetadataPayload) deserializedPayload;
                Assert.assertEquals(deserializedMetadataPayload.getUuid(), metadataPayload.getUuid());
                Assert.assertEquals(deserializedMetadataPayload.getPublicKey(), metadataPayload.getPublicKey());
            } else {
                Assert.fail("Deserialization of polymorphism object failed.");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Assert.fail("KeyPair generation failed.");
        }
    }

    @Test
    public void testPingPayload() {

        try {

            final KeyPair keyPair = Cryptography.generateKeyPair(1024);

            PingPayload pingPayload = new PingPayload("abc");

            final String json = Payload.serialize(pingPayload);


            final Payload deserializedPayload = Payload.deserialize(json);

            if (deserializedPayload instanceof PingPayload) {
                PingPayload deserializedPingPayload = (PingPayload) deserializedPayload;
                Assert.assertEquals(deserializedPingPayload.getMessage(), pingPayload.getMessage());
            } else {
                Assert.fail("Deserialization of polymorphism object failed.");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Assert.fail("KeyPair generation failed.");
        }
    }

    @Test
    public void testSignedPingPayload() {

        try {

            final KeyPair keyPair = Cryptography.generateKeyPair(1024);

            PingPayload pingPayload = new PingPayload("abc");

            SignedPayload signedPayload = new SignedPayload(pingPayload, keyPair.getPrivate());

            final String json = Payload.serialize(signedPayload);

            final Payload deserializedPayload = Payload.deserialize(json);

            // Determine what kind of payload it is.
            if (deserializedPayload instanceof SignedPayload) {
                SignedPayload deserializedSignedPayload = (SignedPayload) deserializedPayload;

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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Assert.fail("KeyPair generation failed.");
        }
    }
}
