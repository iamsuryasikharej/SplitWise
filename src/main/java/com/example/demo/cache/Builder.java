package com.example.demo.cache;

import lombok.Builder;

import java.time.Instant;

@Builder
class Record<V> {
    V v;
    Instant timeStamp;
    Instant loadTime;
    Instant accessedAt;
    int accessCount;

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Record<V> rec = (Record<V>) obj;
        return this.v.equals(rec.v);
    }
}
