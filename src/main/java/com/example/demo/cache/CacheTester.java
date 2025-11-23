package com.example.demo.cache;

import java.util.concurrent.ExecutionException;

public class CacheTester {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        MockDataSource<String, String> mockDataSource = new MockDataSource<>();
        mockDataSource.insert("Surya", "Dev");
        mockDataSource.insert("Mac", "m4");
        mockDataSource.insert("WB", "Kolkata");

        Cache<String, String> c = new Cache();
        c.ds = mockDataSource;

        System.out.println(c.get("Surya").get());
        System.out.println(c.get("Mac").get());
        System.out.println(c.get("Mac").get());

        Thread.sleep(2000);

    }
}
