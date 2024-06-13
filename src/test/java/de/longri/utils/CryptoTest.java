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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CryptoTest {

    @Test
    void decrypt() throws GeneralSecurityException, IOException {
        String str = "qwertyuiop";
        String decrypt = Crypto.encrypt(str);
        assertEquals("yn3/CDhKNHAYSw==", decrypt);
        assertEquals(str, Crypto.decrypt(decrypt));
    }

    @Test
    void testDecrypt() throws GeneralSecurityException, IOException {
        String str = "!test#admin#2021!";
        String decrypt = Crypto.encrypt(str);
        assertEquals(str, Crypto.decrypt(decrypt));
    }

    @Test
    void test() throws GeneralSecurityException, IOException {

        String password = "!test#admin#2021!";

        String originalPassword = "secret";
        String encryptedPassword = Crypto.encrypt(originalPassword, password);
        String decryptedPassword = Crypto.decrypt(encryptedPassword, password);
        assertEquals(originalPassword, decryptedPassword);
    }

    @Test
    void byteArrayTest() {

        int[] KEY = new int[]{2, 18, 123, 2048};
        String PASSWORD = "!test#admin#2021!";

        Random rd = new Random();
        byte[] expectedRandom = new byte[300];
        rd.nextBytes(expectedRandom);

        byte[] encrypt1 = Crypto.encrypt(expectedRandom);
        byte[] encrypt2 = Crypto.encrypt(expectedRandom, KEY);
        byte[] encrypt3 = Crypto.encrypt(expectedRandom, PASSWORD);

        assertFalse(Arrays.equals(expectedRandom, encrypt1));
        assertFalse(Arrays.equals(expectedRandom, encrypt2));
        assertFalse(Arrays.equals(expectedRandom, encrypt3));
        assertFalse(Arrays.equals(encrypt2, encrypt1));
        assertFalse(Arrays.equals(encrypt3, encrypt1));
        assertFalse(Arrays.equals(encrypt2, encrypt3));

        byte[] decrypted1 = Crypto.encrypt(encrypt1);
        byte[] decrypted2 = Crypto.encrypt(encrypt2, KEY);
        byte[] decrypted3 = Crypto.encrypt(encrypt3, PASSWORD);

        assertArrayEquals(expectedRandom, decrypted1);
        assertArrayEquals(expectedRandom, decrypted2);
        assertArrayEquals(expectedRandom, decrypted3);

    }
}