package com.example.demo.cache;

import com.google.common.util.concurrent.Futures;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Cache<K, V> {

    Map<K, V> cache = new ConcurrentHashMap<>();

    public Future<V> set(K k, V v) {
        return CompletableFuture.supplyAsync(() -> {
            cache.put(k, v);
            return cache.get(k);
        });
    }

    public Future<V> get(K k) {
        return CompletableFuture.supplyAsync(() -> cache.get(k));
    }
}
