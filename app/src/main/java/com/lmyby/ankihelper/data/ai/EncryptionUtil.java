package com.lmyby.ankihelper.data.ai;

import android.util.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil {
    private static final String ALGORITHM = "AES";
    private static final String KEY = "AnkiHelperAIKey1"; // 16 characters for 128-bit key

    public static String encrypt(String value) {
        try {
            Key key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedValue = cipher.doFinal(value.getBytes());
            return Base64.encodeToString(encryptedValue, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String encryptedValue) {
        try {
            Key key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedValue = cipher.doFinal(Base64.decode(encryptedValue, Base64.DEFAULT));
            return new String(decryptedValue);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}