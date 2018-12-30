package com.ictreport.ixi.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Cryptography {

    private final static Logger LOGGER = LogManager.getLogger(Cryptography.class);

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

    public static PrivateKey getPrivateKeyFromBytes(final byte[] bytes) throws InvalidKeySpecException {

        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static PublicKey getPublicKeyFromBytes(final byte[] bytes) throws InvalidKeySpecException {

        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] sign(byte[] bytes, PrivateKey key) {

        try {
            Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initSign(key);
            sig.update(bytes);
            return sig.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean verify(byte[] bytes, byte[] signature, PublicKey key) {

        try {
            Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initVerify(key);
            sig.update(bytes);
            return sig.verify(signature);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
        }

        return false;
    }
}
