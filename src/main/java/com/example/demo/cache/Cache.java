package com.example.demo.cache;

import org.apache.catalina.util.Introspection;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class Cache<K, V> {

    MockDataSource<K, V> ds;
    ConcurrentHashMap<K, Record<V>> cache = new ConcurrentHashMap<>();
    WritePloicy writePolicy = WritePloicy.WRITE_THROUGH;
    final ReplacementPolicy policy = ReplacementPolicy.LFU;
    Map<Integer, List<K>> accessCountHashMap = new ConcurrentHashMap<>();

    ConcurrentSkipListMap<AccessDetails, List<K>> mapForEviction = new ConcurrentSkipListMap<>((o1, o2) -> {
        if (policy.equals(ReplacementPolicy.LRU)) {
            return Math.toIntExact(o1.accessedAt.toEpochMilli() - o2.accessedAt.toEpochMilli());
        } else
            return o1.accessCount - o2.accessCount;
    });

    long expiresIn = 60;

    public Future<Void> set(K k, V v) {
        if (cache.containsKey(k)) // if cache contains the key then it is an update ops
        {
            if (writePolicy.equals(WritePloicy.WRITE_THROUGH)) { // first update the db if successful then go on and
                // update cache sync
                return ds.insert(k, v).thenAccept(__ -> cache.put(k,
                        newRecord(v, cache.get(k).loadTime, cache.get(k).accessDetails.accessCount)));
            } else { // write back first update the cache and then go on update the db async
                cache.put(k, newRecord(v, Instant.now(), 1));
                // Record.<V>
                // builder().v(v).accessCount(1).accessedAt(Instant.now()).loadTime(Instant.now()).build());
                ds.insert(k, v);
                return CompletableFuture.completedFuture(null);
            }
            // Record.<V>
            // builder().v(v).accessCount(cache.get(k).accessCount).loadTime(cache.get(k).loadTime).accessedAt(Instant.now()).build()
        } else if (cache.size() >= 5) { // replacement
            if (policy.equals(ReplacementPolicy.LFU)) {

                cache.remove(mapForEviction.firstEntry().getValue().get(0));
                mapForEviction.get(mapForEviction.firstKey()).remove(0);
                if (mapForEviction.get(mapForEviction.firstKey()).isEmpty())
                    mapForEviction.remove(mapForEviction.firstKey());

                if (writePolicy == WritePloicy.WRITE_THROUGH) { // Enums should always be compared using ==
                    return ds.insert(k, v).thenAccept(__ -> cache.put(k, newRecord(v, Instant.now(), 1)));
                    // Record.<V>
                    // builder().v(v).accessCount(1).loadTime(Instant.now()).accessedAt(Instant.now()).build()));
                } else {
                    cache.put(k, newRecord(v, Instant.now(), 1));
                    // Record.<V>
                    // builder().v(v).accessCount(1).accessedAt(Instant.now()).loadTime(Instant.now()).build());
                    ds.insert(k, v);
                    return CompletableFuture.completedFuture(null);
                }

            }
            // replacement algo
        } else if (writePolicy == WritePloicy.WRITE_THROUGH) { // Enums should always be compared using ==
            return ds.insert(k, v).thenAccept(__ -> cache.put(k, newRecord(v, Instant.now(), 1)));
            // Record.<V>
            // builder().v(v).accessCount(1).loadTime(Instant.now()).accessedAt(Instant.now()).build()));
        } else {
            cache.put(k, newRecord(v, Instant.now(), 1));
            // Record.<V>
            // builder().v(v).accessCount(1).accessedAt(Instant.now()).loadTime(Instant.now()).build());
            ds.insert(k, v);
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(null);
    }

    private Record<V> newRecord(V v, Instant loadTime, int accessCount) {
        return Record.<V> builder().v(v).accessDetails(new AccessDetails(accessCount, Instant.now())).loadTime(loadTime)
                .build();
    }

    public Future<V> get(K k) {
        if (cache.containsKey(k) && cache.get(k).loadTime.toEpochMilli() >= System.currentTimeMillis()
                - Instant.ofEpochSecond(expiresIn).toEpochMilli()) {

            return CompletableFuture.runAsync(() -> {
                Record<V> r = cache.get(k);
                if (mapForEviction.get(r.accessDetails) != null) {
                    mapForEviction.get(r.accessDetails).remove(k);
                    if (mapForEviction.get(r.accessDetails).isEmpty())
                        mapForEviction.remove(r.accessDetails);
                }

                Record<V> newRecord = new Record<>(r);
                addAccessCountMetaDataV(newRecord.accessDetails, k);
                cache.put(k, newRecord);

            }).thenApply(__ -> cache.get(k).v);
        } else {
            //
            return ds.get(k).thenAccept(v1 -> {
                cache.put(k, Record.<V> builder().v(v1).accessDetails(new AccessDetails(1, Instant.now()))
                        .loadTime(Instant.now()).v(v1).build());

                if (accessCountHashMap.get(cache.get(k).accessDetails.accessCount) != null) {
                    accessCountHashMap.get(cache.get(k).accessDetails.accessCount).add(k);
                } else {
                    accessCountHashMap.put(cache.get(k).accessDetails.accessCount,
                            new CopyOnWriteArrayList<>(List.of(k)));
                }

                addAccessCountMetaDataV(cache.get(k).accessDetails, k);

            }).thenApply(x3 -> (cache.get(k).v));
        }
    }

    public void addAccessCountMetaDataV(AccessDetails accessDetails, K k) {
        if (mapForEviction.get(accessDetails) != null) {
            mapForEviction.get(accessDetails).remove(k);
            if (mapForEviction.get(accessDetails).isEmpty()) {
                mapForEviction.remove(accessDetails);
            }
        }
        if (mapForEviction.containsKey(accessDetails)) {
            mapForEviction.get(accessDetails).add(k);
        } else {

            mapForEviction.put(accessDetails, new ArrayList<>(List.of(k)));
        }

    }

}
