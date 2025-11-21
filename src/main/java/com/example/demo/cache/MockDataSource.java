package com.example.demo.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MockDataSource<K,V> {
    Map<K, V> ds = new HashMap<>();
    public CompletableFuture<V> insert(K k, V v)
    {
        return CompletableFuture.completedFuture(ds.put(k,v));
    }
    public void update(K k,V v)
    {
        ds.put(k,v);
    }

    public CompletableFuture<V> get(K k)
    {
        return CompletableFuture.completedFuture(ds.get(k));
    }

}
