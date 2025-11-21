package com.example.demo.cache;

import com.sun.jdi.VoidType;
import lombok.Builder;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;



public class Cache<K,V> {

    MockDataSource<K, V> ds;
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

    Map<Instant,List<Record<V>>>  listByAccessTime=new ConcurrentSkipListMap<>(accessTimeTimeComparator);
    Map<Integer,List<Record<V>>>  listByAccessCount=new ConcurrentSkipListMap<>(accessCountTimeComparator);

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
                                    Record.<V>builder().v(v).accessCount(0).timeStamp(Instant.now()).loadTime(Instant.now()).build())
                            );
        } else {
            cache.put(k, Record.<V>builder().v(v).accessCount(0).timeStamp(Instant.now()).loadTime(Instant.now()).build());
            ds.insert(k, v);
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(null);
    }

    public Future<V> get(K k) {
        if (cache.containsKey(k) && cache.get(k).timeStamp.toEpochMilli() + Instant.ofEpochSecond(expiresIn).toEpochMilli() >= System.currentTimeMillis()) {
            oldMetadataUpdate(k);
            updateMetaData(k);

            cache.get(k).accessCount++;
            return CompletableFuture.completedFuture(cache.get(k).v);
        } else {
            return ds.get(k)
                    .thenApply(v1 ->
                            cache.put(k, Record.<V>builder().v(v1).accessCount(0).timeStamp(Instant.now()).loadTime(Instant.now()).v(v1).build())).thenApply(x3 ->
                    {
                        oldMetadataUpdate(k);
                        updateMetaData(k);


                        return (cache.get(k).v);
                    });
        }
    }



    public void updateMetaData(K k)
    {
        var keyToCheck=cache.get(k).accessCount+1;
        if(listByAccessCount.get(keyToCheck)!=null)
        {
            var key=cache.get(k).accessCount+1;
            cache.get(k).accessCount++;
            var list=listByAccessCount.get(key);
            if(list!=null) {
                list.add(cache.get(k));
                listByAccessCount.put(key,list);
            }
            else {
                List<Record<V>> list2=new ArrayList<>();
                list2.add(cache.get(k));
                cache.get(k).accessCount++;
                listByAccessCount.put(key,list2);
            }

        }
        else {

            List<Record<V>> list=new ArrayList<>();
            var key=cache.get(k).accessCount+1;
            list.add(cache.get(k));
            listByAccessCount.put(key,list);
        }

        if(listByAccessTime.get(Instant.now()) != null)
        {
            listByAccessTime.get(Instant.now()).add(cache.get(k));
        }
        else {
            List<Record<V>> list=new ArrayList<>();
            list.add(cache.get(k));
            listByAccessTime.put(Instant.now(), list);
        }
    }
    public void oldMetadataUpdate(K k)
    {
        if(listByAccessCount.containsKey(cache.get(k).accessCount))
        {
            listByAccessCount
                    .get(cache.get(k).accessCount)
                    .stream().forEach(v->{
                        if(v.v==cache.get(k).v){
                            listByAccessCount
                                    .get(cache.get(k).accessCount)
                                    .remove(v);
                        }
                    });
        }
    }
}
