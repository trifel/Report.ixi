package com.ictreport.ixi.utils;

import org.junit.Assert;
import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class CryptographyTest {

    @Test
    public void testAuthenticityIntegrity() {
        final KeyPair keyPair = Cryptography.generateKeyPair(Constants.KEY_LENGTH);
        final String message = "Testing authenticity and integrity";
        final String encryptedMessage = Cryptography.encryptText(message, keyPair.getPrivate());
        final String decryptedMessage = Cryptography.decryptText(encryptedMessage, keyPair.getPublic());

        Assert.assertEquals("Message and decrypted message should be equal", message, decryptedMessage);
    }

    @Test
    public void testConfidentiality() {
        final KeyPair keyPair = Cryptography.generateKeyPair(Constants.KEY_LENGTH);
        final String message = "Testing confidentiality";
        final String encryptedMessage = Cryptography.encryptText(message, keyPair.getPublic());
        final String decryptedMessage = Cryptography.decryptText(encryptedMessage, keyPair.getPrivate());

        Assert.assertEquals("Message and decrypted message should be equal", message, decryptedMessage);
    }

    @Test
    public void testPrivateKeySerialization() {
        final KeyPair keyPair = Cryptography.generateKeyPair(Constants.KEY_LENGTH);
        final byte[] encodedPrivateKey = keyPair.getPrivate().getEncoded();
        final PrivateKey decodedPrivateKey = Cryptography.getPrivateKeyFromBytes(encodedPrivateKey);

        Assert.assertEquals("Failed to (de)serialize private key",keyPair.getPrivate(), decodedPrivateKey);
    }

    @Test
    public void testPublicKeySerialization() {
        final KeyPair keyPair = Cryptography.generateKeyPair(Constants.KEY_LENGTH);
        final byte[] encodedPublicKey = keyPair.getPublic().getEncoded();
        final PublicKey decodedPublicKey = Cryptography.getPublicKeyFromBytes(encodedPublicKey);

        Assert.assertEquals("Failed to (de)serialize public key", keyPair.getPublic(), decodedPublicKey);
    }

    @Test
    public void testSignAndVerify() {
        final KeyPair keyPair = Cryptography.generateKeyPair(Constants.KEY_LENGTH);
        final byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        final byte[] signature = Cryptography.sign(data, keyPair.getPrivate());
        final boolean verifyResult = Cryptography.verify(data, signature, keyPair.getPublic());

        Assert.assertTrue("Failed to sign and verify data", verifyResult);
    }
}
