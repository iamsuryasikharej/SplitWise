package com.example.demo.cache;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class Cache<K, V> {

    MockDataSource<K, V> ds;
    ConcurrentHashMap<K, Record<V>> cache = new ConcurrentHashMap<>();
    WritePloicy writePolicy = WritePloicy.WRITE_THROUGH;
    final ReplacementPolicy policy = ReplacementPolicy.LFU;
//    ExecutorService[] es=new ExecutorService[3];
    Map<Integer, List<K>> accessCountHashMap=new ConcurrentHashMap<>();

    Comparator<Instant> accessTimeTimeComparator = new Comparator<Instant>() {
        @Override
        public int compare(Instant o1, Instant o2) {
            return Long.compare(o1.toEpochMilli(), o2.toEpochMilli());
        }
    };

    Comparator<Integer> accessCountTimeComparator = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return Integer.compare(o1.intValue(), o2.intValue());
        }
    };

    Map<Instant, List<Record<V>>> listByAccessTime = new ConcurrentSkipListMap<>(accessTimeTimeComparator);
    Map<Integer, List<Record<V>>> listByAccessCount = new ConcurrentSkipListMap<>(accessCountTimeComparator);

    long expiresIn = 60;

    public Future<Void> set(K k, V v) {
        if(cache.containsKey(k)) // if cache contains the key then it is an update ops
        {
            if (writePolicy.equals(WritePloicy.WRITE_THROUGH)) { // first update the db if successful then go on and update cache sync
                return ds.insert(k, v).thenAccept(__ -> cache.put(k, newRecord(v, cache.get(k).loadTime, cache.get(k).accessDetails.accessCount)));
            }
            else { // write back first update the cache and then go on update the db async
                cache.put(k,newRecord(v,Instant.now(),1));
//                    Record.<V> builder().v(v).accessCount(1).accessedAt(Instant.now()).loadTime(Instant.now()).build());
                ds.insert(k, v);
                return CompletableFuture.completedFuture(null);
            }
//                            Record.<V> builder().v(v).accessCount(cache.get(k).accessCount).loadTime(cache.get(k).loadTime).accessedAt(Instant.now()).build()
        }
        else if (cache.size() >= 5) { //replacement
            if (policy.equals(ReplacementPolicy.LFU)) {
                boolean finder=false;
                int x=0;
                List<K> toRemove = null;
                while(!finder)
                {
                    toRemove=accessCountHashMap.get(x);
                    if(toRemove!=null &&!toRemove.isEmpty())
                        finder=true;
                    x++;
                }
                System.out.println("removed"+toRemove.get(0));
                cache.remove(toRemove.get(0));
                toRemove.remove(0);


                if (writePolicy == WritePloicy.WRITE_THROUGH) { //Enums should always be compared using ==
                    return ds.insert(k, v).thenAccept(__ -> cache.put(k,newRecord(v,Instant.now(),1)));
//                            Record.<V> builder().v(v).accessCount(1).loadTime(Instant.now()).accessedAt(Instant.now()).build()));
                }
                else {
                    cache.put(k,newRecord(v,Instant.now(),1));
//                            Record.<V> builder().v(v).accessCount(1).accessedAt(Instant.now()).loadTime(Instant.now()).build());
                    ds.insert(k, v);
                    return CompletableFuture.completedFuture(null);
                }

            }
            // replacement algo
        } else if (writePolicy == WritePloicy.WRITE_THROUGH) { //Enums should always be compared using ==
            return ds.insert(k, v).thenAccept(__ -> cache.put(k, newRecord(v,Instant.now(),1)));
//                    Record.<V> builder().v(v).accessCount(1).loadTime(Instant.now()).accessedAt(Instant.now()).build()));
        } else {
            cache.put(k,newRecord(v,Instant.now(),1));
//                    Record.<V> builder().v(v).accessCount(1).accessedAt(Instant.now()).loadTime(Instant.now()).build());
            ds.insert(k, v);
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(null);
    }

//    public Future<V> get(K k) {
//        if (cache.containsKey(k) && cache.get(k).loadTime.toEpochMilli()
//                + Instant.ofEpochSecond(expiresIn).toEpochMilli() >= System.currentTimeMillis()) {
//            addAccessCountMetaData(k);
//            Record<V> r=cache.get(k);
//            Record<V> newRecord=new Record<>(r);
//            cache.put(k,newRecord);
//            return CompletableFuture.completedFuture(r.v);
//        } else {
////            if(cache.containsKey(k))
////            {
////                accessCountHashMap.get(cache.get(k).accessCount).remove(k);
////
////            }
//            return ds.get(k).thenApply(v1 -> {cache.put(k, Record.<V> builder().v(v1).accessCount(1).accessedAt(Instant.now())
//                    .loadTime(Instant.now()).v(v1).build());
////                addAccessCountMetaData(k);
//                if (accessCountHashMap.get(cache.get(k).accessCount)!=null)
//                {
//                    accessCountHashMap.get(cache.get(k).accessCount).add(k);
//                }
//                else{
//                    accessCountHashMap.put(cache.get(k).accessCount,new CopyOnWriteArrayList<>(List.of(k)));
//                }
//                return v1;}).thenApply(x3 -> {
//                        return (cache.get(k).v);
//                    });
//        }
//    }

    public void addAccessCountMetaData(K k)
    {
        accessCountHashMap.get(cache.get(k).accessDetails.accessCount).remove(k);
        if(accessCountHashMap.get(cache.get(k).accessDetails.accessCount+1)!=null)
        {
            accessCountHashMap.get(cache.get(k).accessDetails.accessCount+1).add(k);
        }
        else{
            accessCountHashMap.put(cache.get(k).accessDetails.accessCount+1,new CopyOnWriteArrayList<>(List.of(k)));
        }
    }


    private Record<V> newRecord(V v, Instant loadTime, int accessCount) {
        return Record.<V>builder()
                .v(v)
                .accessDetails(new AccessDetails(accessCount,Instant.now()))
                .loadTime(loadTime)
                .build();
    }



    public Future<V> get(K k) {
        if (cache.containsKey(k) && cache.get(k).loadTime.toEpochMilli()
                >= System.currentTimeMillis() - Instant.ofEpochSecond(expiresIn).toEpochMilli()) {
//            addAccessCountMetaData(k);
//            Record<V> r=cache.get(k);
//            Record<V> newRecord=new Record<>(r);
//            cache.put(k,newRecord);
//            return CompletableFuture.completedFuture(r.v);

            // ?
            return CompletableFuture.runAsync(()->{

                addAccessCountMetaData(k);
                Record<V> r=cache.get(k);
            Record<V> newRecord=new Record<>(r);
            cache.put(k,newRecord);

            }).thenApply(__->cache.get(k).v);
        } else {
//
            return ds.get(k).thenAccept(v1 -> {cache.put(k, Record.<V> builder().v(v1).accessDetails(new AccessDetails(1,Instant.now()))
                    .loadTime(Instant.now()).v(v1).build());

                if (accessCountHashMap.get(cache.get(k).accessDetails.accessCount)!=null)
                {
                    accessCountHashMap.get(cache.get(k).accessDetails.accessCount).add(k);
                }
                else{
                    accessCountHashMap.put(cache.get(k).accessDetails.accessCount,new CopyOnWriteArrayList<>(List.of(k)));
                }

//                addAccessCountMetaData(k);
                }).thenApply(x3 -> (cache.get(k).v));
        }
    }

}


