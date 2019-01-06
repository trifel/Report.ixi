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
    public static KeyPair generateKeyPair(final int length) {
        final KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(length);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate key pair!", e);
        }
    }

    /**
     * Encrypts a text
     * @param data
     * @param key
     * @return an encrypted text
     */
    public static String encryptText(final String data, final Key key) {
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.encodeBase64String(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException | NoSuchPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts a text
     * @param text
     * @param key
     * @return a decrypted text
     */
    public static String decryptText(final String text, final Key key) {
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.decodeBase64(text)), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException |
                BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    public static PrivateKey getPrivateKeyFromBytes(final byte[] bytes) {
        try {
            final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get private key from bytes", e);
        }
    }

    public static PublicKey getPublicKeyFromBytes(final byte[] bytes) {
        try {
            final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get public key from bytes", e);
        }
    }

    public static byte[] sign(final byte[] bytes, final PrivateKey key) {
        try {
            final Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initSign(key);
            sig.update(bytes);
            return sig.sign();
        } catch (final NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to sign bytes", e);
        }
    }

    public static boolean verify(final byte[] bytes, final byte[] signature, final PublicKey key) {
        try {
            final Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initVerify(key);
            sig.update(bytes);
            return sig.verify(signature);
        } catch (final InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }
}
