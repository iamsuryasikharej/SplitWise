package com.example.demo.cache;

public class Test {
    public static void main(String[] args) {
        for (int i = 0; i < 300000000; i++) {
            Thread t = new Thread(() -> System.out.println(Thread.currentThread().getName()));
            t.start();
        }
    }
}
