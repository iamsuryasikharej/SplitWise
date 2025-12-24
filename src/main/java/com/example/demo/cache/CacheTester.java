package com.example.demo.cache;

import org.apache.tomcat.util.net.openssl.ciphers.Encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class CacheTester {
    public static void main(String[] args) throws InterruptedException, ExecutionException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        MockDataSource<String, String> mockDataSource = new MockDataSource<>();
        mockDataSource.insert("Surya", "Dev");
        mockDataSource.insert("Mac", "m4");
        mockDataSource.insert("WB", "Kolkata");

        Cache<String, String> c = new Cache<>();
        c.ds = mockDataSource;

        System.out.println(c.get("Surya").get());
        System.out.println(c.get("Mac").get());
        System.out.println(c.get("Mac").get());
        System.out.println(c.get("Surya").get());
        System.out.println(c.get("Mac").get());
        System.out.println(c.get("Surya").get());
        System.out.println(c.get("Surya").get());

        System.out.println(c.get("Mac").get());

        c.set("silicon", "M1");
        c.set("snapdragon", "x-elite");
        c.set("Mediatek", "Arm");
        c.set("AMD", "ThreadRipper");
        c.set("exynos", "234");
        System.out.println(c.get("exynos").get());
        System.out.println(c.get("exynos").get());

        System.out.println(c.cache);
        Thread.sleep(2000);

    }
}
