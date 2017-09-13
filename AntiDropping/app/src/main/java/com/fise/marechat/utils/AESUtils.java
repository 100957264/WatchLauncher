package com.fise.marechat.utils;


import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//import java.util.Base64;

/**
 * 提供加密解密工具方法
 */
public class AESUtils {

    private static final String TAG = "AESUtils";

    /**
     * 加密
     * @param key 秘钥
     * @param stringToEncode 待加密的字符串
     * @return 加密后的字符串
     */
    public static String encode(String key, String stringToEncode) {

        try {
            SecretKeySpec skeySpec = getKey(key);
            byte[] clearText = stringToEncode.getBytes("UTF8");

            String ivStr = key.substring(0, 16);
            byte[] passwordBytes = ivStr.getBytes("UTF-8");
            byte[] iv = new byte[16];
            Arrays.fill(iv, (byte) 0x00);
            System.arraycopy(passwordBytes, 0, iv, 0, 16);

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);

            return Base64.encodeToString(cipher.doFinal(clearText), Base64.DEFAULT);
        }catch (Exception e){
            LogUtils.e(TAG, "encode", e);
        }
        return "";
    }

    /**
     * 解密
     * @param key 秘钥
     * @param stringToDecode 待解密的字符串
     * @return 解密后的字符串
     */
    public static String decode(String key, String stringToDecode) {

        try {
            SecretKeySpec skeySpec = getKey(key);
            byte[] clearText = Base64.decode(stringToDecode, Base64.DEFAULT);

            String ivStr = key.substring(0, 16);
            byte[] passwordBytes = ivStr.getBytes("UTF-8");
            byte[] iv = new byte[16];
            Arrays.fill(iv, (byte) 0x00);
            System.arraycopy(passwordBytes, 0, iv, 0, 16);

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);

            return new String(cipher.doFinal(clearText), "UTF8");
        } catch (Exception e){
            LogUtils.e(TAG, "decode", e);
        }
        return "";
    }

    private static SecretKeySpec getKey(String password) throws UnsupportedEncodingException {
        int keyLength = 256;
        byte[] keyBytes = new byte[keyLength / 8];
        Arrays.fill(keyBytes, (byte) 0x0);
        byte[] passwordBytes = password.getBytes("UTF-8");
        int length = passwordBytes.length < keyBytes.length ? passwordBytes.length : keyBytes.length;
        System.arraycopy(passwordBytes, 0, keyBytes, 0, length);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        return key;
    }

}
