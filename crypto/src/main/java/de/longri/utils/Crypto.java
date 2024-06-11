/*
 * Copyright (C) 2024 Longri
 *
 * This file is part of fxutils.
 *
 * fxutils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * fxutils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with fxutils. If not, see <https://www.gnu.org/licenses/>.
 */
package de.longri.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class Crypto {

    public static final String INVISIBLE = "\u200e";

    static final int[] KEY = {128, 56, 20, 78, 33, 225};

    public static String decrypt(String value) throws GeneralSecurityException, IOException {
        return decrypt(value, KEY);
    }

    public static String decrypt(String value, int[] key) throws GeneralSecurityException, IOException {
        int[] b = null;
        try {
            b = byte2intArray(Base64.getDecoder().decode(value));
        } catch (Exception e) {

            if (e instanceof IllegalArgumentException && e.getMessage().contains("Illegal base64 character")) {
                // use other decrypt
                String pw = "";
                for (int i : key) pw += String.valueOf(i);
                return INVISIBLE + decrypt(value, pw);
            }
            e.printStackTrace();
        }

        rc4(b, key);
        String decrypted = "";

        char[] c = new char[b.length];
        for (int x = 0; x < b.length; x++) {
            c[x] = (char) b[x];
        }

        decrypted = String.copyValueOf(c);

        return decrypted;

    }

    public static String decrypt(String value, String password) throws GeneralSecurityException, IOException {
        return decryptIllegalChar(value, createSecretKey(password.toCharArray(), salt, 40000, 128));
    }

    public static String encrypt(String value) throws GeneralSecurityException, UnsupportedEncodingException {
        return encrypt(value, KEY);
    }

    public static String encrypt(String value, int[] key) throws GeneralSecurityException, UnsupportedEncodingException {

        String encrypted = "";
        try {
            int[] b = byte2intArray(value.getBytes());
            rc4(b, key);
            encrypted = Base64.getEncoder().encodeToString(int2byteArray(b));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    public static String encrypt(String value, String password) throws GeneralSecurityException, UnsupportedEncodingException {
        return encryptIllegalChar(value, createSecretKey(password.toCharArray(), salt, 40000, 128));
    }

    private static final byte[] salt = new String(Crypto.class.getPackageName() + "." + Crypto.class.getName()).getBytes();

    private static SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey keyTmp = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(keyTmp.getEncoded(), "AES");
    }

    private static String encryptIllegalChar(String property, SecretKeySpec key) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
        AlgorithmParameters parameters = pbeCipher.getParameters();
        IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
        byte[] cryptoText = pbeCipher.doFinal(property.getBytes("UTF-8"));
        byte[] iv = ivParameterSpec.getIV();
        return base64Encode(iv) + ":" + base64Encode(cryptoText);
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String decryptIllegalChar(String string, SecretKeySpec key) throws GeneralSecurityException, IOException {
        String iv = string.split(":")[0];
        String property = string.split(":")[1];
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(iv)));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }

    private static byte[] base64Decode(String property) throws IOException {
        return Base64.getDecoder().decode(property);
    }

    public static void rc4(int[] bytes, int[] key) {
        int[] s = new int[256];
        int[] k = new int[256];
        int temp;
        int i, j;

        for (i = 0; i < 256; i++) {
            s[i] = (int) i;
            k[i] = (int) key[i % key.length];
        }

        j = 0;
        for (i = 0; i < 256; i++) {
            j = (j + s[i] + k[i]) % 256;
            temp = s[i];
            s[i] = s[j];
            s[j] = temp;
        }

        i = j = 0;
        for (int x = 0; x < bytes.length; x++) {
            i = (i + 1) % 256;
            j = (j + s[i]) % 256;
            temp = s[i];
            s[i] = s[j];
            s[j] = temp;
            int t = (s[i] + s[j]) % 256;
            bytes[x] = (int) (bytes[x] ^ s[t]);
        }
    }

    private static int[] byte2intArray(byte[] b) {
        int[] i = new int[b.length];

        for (int x = 0; x < b.length; x++) {
            int t = b[x];
            if (t < 0) {
                t += 256;
            }
            i[x] = t;
        }

        return i;
    }

    private static byte[] int2byteArray(int[] i) {
        byte[] b = new byte[i.length];

        for (int x = 0; x < i.length; x++) {

            int t = i[x];
            if (t > 128) {
                t -= 256;
            }

            b[x] = (byte) t;
        }

        return b;
    }

    public static byte[] decrypt(byte[] bytes) {
        return decrypt(bytes, KEY);
    }

    public static byte[] decrypt(byte[] bytes, String key) {
        return decrypt(bytes, byte2intArray(key.getBytes()));
    }

    public static byte[] decrypt(byte[] bytes, int[] key) {
        int[] b = byte2intArray(bytes);
        rc4(b, key);
        return int2byteArray(b);
    }

    public static byte[] encrypt(byte[] bytes) {
        return encrypt(bytes, KEY);
    }

    public static byte[] encrypt(byte[] bytes, String key) {
        return encrypt(bytes, byte2intArray(key.getBytes()));
    }

    public static byte[] encrypt(byte[] bytes, int[] key) {
        int[] b = byte2intArray(bytes);
        rc4(b, key);
        return int2byteArray(b);
    }

}
