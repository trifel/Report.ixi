package com.ictreport.ixi.utils;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class CryptographyTest {

    @Test
    public void testAuthenticityIntegrity() {
        try {
            KeyPair keyPair = Cryptography.generateKeyPair(1024);
            final String message = "Testing authenticity and integrity";

            final String encryptedMessage = Cryptography.encryptText(message, keyPair.getPrivate());
            final String decryptedMessage = Cryptography.decryptText(encryptedMessage, keyPair.getPublic());

            Assert.assertEquals("Message and decrypted message should be equal", message, decryptedMessage);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConfidentiality() {
        try {
            KeyPair keyPair = Cryptography.generateKeyPair(1024);
            final String message = "Testing confidentiality";

            final String encryptedMessage = Cryptography.encryptText(message, keyPair.getPublic());
            final String decryptedMessage = Cryptography.decryptText(encryptedMessage, keyPair.getPrivate());

            Assert.assertEquals("Message and decrypted message should be equal", message, decryptedMessage);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPrivateKeySerialization() {
        try {
            KeyPair keyPair = Cryptography.generateKeyPair(1024);
            byte[] encodedPrivateKey = keyPair.getPrivate().getEncoded();
            byte[] encodedPublicKey = keyPair.getPublic().getEncoded();

            PrivateKey decodedPrivateKey = null;
            PublicKey decodedPublicKey = null;

            try {
                decodedPrivateKey = Cryptography.getPrivateKeyFromBytes(encodedPrivateKey);

            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }

            try {
                decodedPublicKey = Cryptography.getPublicKeyFromBytes(encodedPublicKey);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }

            Assert.assertEquals(keyPair.getPrivate(), decodedPrivateKey);
            Assert.assertEquals(keyPair.getPublic(), decodedPublicKey);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSignAndVerify() {

        try {
            KeyPair keyPair = Cryptography.generateKeyPair(1024);
            final byte[] data = "test".getBytes(StandardCharsets.UTF_8);

            final byte[] signature = Cryptography.sign(data, keyPair.getPrivate());
            System.out.println("Signature:" + Base64.encodeBase64String(signature));

            final boolean verifyResult = Cryptography.verify(data, signature, keyPair.getPublic());

            Assert.assertTrue(verifyResult);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
