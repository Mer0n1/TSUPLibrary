package com.tsup.crypto;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.spec.IvParameterSpec;

public class AEADUtils {

    public static final String KEY_ALGORITHM = "AES"; //ChaCha20
    public static final String KEY_TRANSFORMATION = "AES/GCM/NoPadding"; //ChaCha20-Poly1305
    public static final int keySize = 256;//bits
    public static final int AuthTagSize = 128; //bits
    public static final int NonceSize = 12; //bytes

    public static byte[] decryptBASE64(String key) {
        return Base64.getDecoder().decode(key);
    }

    public static String encryptBASE64(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }

    public static byte[] encrypt(byte[] data, byte[] iv, SecretKey key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(KEY_TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(AuthTagSize, iv); //AES
        //IvParameterSpec spec = new IvParameterSpec(iv); //ChaCha
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data, byte[] iv, SecretKey key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(KEY_TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(AuthTagSize, iv); //AES
        //IvParameterSpec ivSpec = new IvParameterSpec(iv); //ChaCha
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        return cipher.doFinal(data);
    }

    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(keySize);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static byte[] generateIv() {
        byte[] iv = new byte[NonceSize];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

}
