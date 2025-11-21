package com.example.demo.cache;

import com.sun.jdi.VoidType;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Cache<K,V> {

    MockDataSource<K, V> ds = new MockDataSource();

    ConcurrentHashMap<K, Record<V>> cache = new ConcurrentHashMap<>();
    WritePloicy writePloicy = WritePloicy.WRITE_THROUGH;
    ReplacementPolicy policy = ReplacementPolicy.LFU;

    long expiresIn = 60;

    public Future<Void> set(K k, V v) {
        if (cache.size() >= 500) {
            // replacement algo
        } else if (writePloicy.equals(WritePloicy.WRITE_THROUGH)) {
            return ds
                    .insert(k, v)
                    .thenAccept(__ -> cache.put(k,
                                    Record.<V>builder().v(v).accessCount(1).timeStamp(Instant.now()).build())
                            );
        } else {
            cache.put(k, Record.<V>builder().v(v).accessCount(1).timeStamp(Instant.now()).build());
            ds.insert(k, v);
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(null);
    }

    public Future<V> get(K k, V v) {
        if (cache.containsKey(k) && cache.get(k).timeStamp.toEpochMilli() + Instant.ofEpochSecond(expiresIn).toEpochMilli() >= System.currentTimeMillis()) {
            cache.get(k).accessCount++;
            return CompletableFuture.completedFuture(cache.get(k).v);
        } else {
            return ds.get(k).thenApply(v1 -> cache.put(k, Record.<V>builder().v(v1).accessCount(1).timeStamp(Instant.now()).build())).thenApply(x3 -> x3.v);
        }
    }

    @Builder
    class Record<V> {
        V v;
        Instant timeStamp;
        int accessCount;
    }
}
