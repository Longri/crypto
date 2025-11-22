package de.longri.utils;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UTILS {

    public static void copyFile(File source, File dest) throws IOException {
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(dest)
        ) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    @Test
    public void getSysInfoTest(){

        //alte methode braucht 15921 milisec

        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            UTIL.getSystemInfo();
        }

        UTIL.getSystemInfo()
                .forEach((key, value) -> System.out.println(key + " : " + value));
        System.out.println("Time: " + (System.currentTimeMillis() - start));


    }
}
