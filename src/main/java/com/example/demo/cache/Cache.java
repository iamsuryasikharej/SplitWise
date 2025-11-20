package com.example.demo.cache;

import com.sun.jdi.VoidType;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Cache<K, V> {

    MockDataSource<K,V> ds=new MockDataSource();

    ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();
    WritePloicy writePloicy=WritePloicy.WRITE_THROUGH;
    ReplacementPolicy policy=ReplacementPolicy.LFU;

    public Future<Void> set(K k, V v) {
        if(cache.size()>=500)
        {
            // replacement algo

        }
        else if(writePloicy.equals(WritePloicy.WRITE_THROUGH)) {
            return ds
                    .insert(k,v)
                    .thenAccept(__->cache.put(k,v));
        }
        else{
            cache.put(k,v);
            ds.insert(k,v);
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(null);
    }

    public Future<V> get(K k, V v) {
       if(cache.get(v)!=null) {
           return CompletableFuture.completedFuture(cache.get(k));
       }
       else {
           return ds.get(k);
       }


    }
}
