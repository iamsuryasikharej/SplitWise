package com.example.demo.cache;

import com.sun.jdi.VoidType;
import lombok.Builder;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class Cache<K,V> {

    MockDataSource<K, V> ds = new MockDataSource();



    ConcurrentHashMap<K, Record<V>> cache = new ConcurrentHashMap<>();
    WritePloicy writePloicy = WritePloicy.WRITE_THROUGH;
    final ReplacementPolicy policy = ReplacementPolicy.LFU;
    Comparator<Instant> accessTimeTimeComparator=new Comparator<Instant>() {
        @Override
        public int compare(Instant o1, Instant o2) {
                return Long.compare(o1.toEpochMilli(), o2.toEpochMilli());
        }
    };

    Comparator<Integer> accessCountTimeComparator=new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return Integer.compare(o1.intValue(),o2.intValue());
        }
    };

    Map<Instant,List<Record<V>>>  list=new ConcurrentSkipListMap<>(accessTimeTimeComparator);
    Map<Integer,List<Record<V>>>  listByAcccessCount=new ConcurrentSkipListMap<>(accessCountTimeComparator);

    long expiresIn = 60;

    public Future<Void> set(K k, V v) {
        if (cache.size() >= 500) {
            if(policy.equals(ReplacementPolicy.LFU))
            {

            }
            // replacement algo
        } else if (writePloicy.equals(WritePloicy.WRITE_THROUGH)) {
            return ds
                    .insert(k, v)
                    .thenAccept(__ -> cache.put(k,
                                    Record.<V>builder().v(v).accessCount(1).timeStamp(Instant.now()).loadTime(Instant.now()).build())
                            );
        } else {
            cache.put(k, Record.<V>builder().v(v).accessCount(1).timeStamp(Instant.now()).loadTime(Instant.now()).build());
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
            return ds.get(k)
                    .thenApply(v1 ->
                            cache.put(k, Record.<V>builder().v(v1).accessCount(1).timeStamp(Instant.now()).loadTime(Instant.now()).build())).thenApply(x3 -> v);
        }
    }

    @Builder
    class Record<V> {
        V v;
        Instant timeStamp;
        Instant loadTime;
        Instant accessedAt;
        int accessCount;
    }
}
