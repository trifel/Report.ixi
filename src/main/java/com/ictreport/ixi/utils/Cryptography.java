package com.ictreport.ixi.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class Cryptography {

    /**
     * Generates a new key pair
     * @param length
     * @return a keypair
     */
    public static KeyPair generateKeyPair(int length)
            throws NoSuchAlgorithmException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(length);
        return keyGen.generateKeyPair();
    }

    /**
     * Encrypts a text
     * @param text
     * @param key
     * @return an encrypted text
     */
    public static String encryptText(String text, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.encodeBase64String(cipher.doFinal(text.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Decrypts a text
     * @param text
     * @param key
     * @return a decrypted text
     */
    public static String decryptText(String text, Key key)
            throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            NoSuchPaddingException, NoSuchAlgorithmException {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.decodeBase64(text)), StandardCharsets.UTF_8);
    }
}
