package com.ictreport.ixi.utils;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

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
}
