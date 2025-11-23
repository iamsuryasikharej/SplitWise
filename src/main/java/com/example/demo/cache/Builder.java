package com.example.demo.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.Objects;

@Builder
@AllArgsConstructor
class Record<V> {
    final V v;
    final Instant timeStamp;
    final Instant loadTime;
    final Instant accessedAt;
    final int accessCount;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Record<?> rec = (Record<?>) obj;
        return Objects.equals(this.v, rec.v);
    }

    public Record(Record<V> r) {
        this.v = r.v;
        this.timeStamp = r.timeStamp;
        this.loadTime = r.loadTime;
        this.accessedAt = Instant.now();
        this.accessCount = r.accessCount+1;
    }


    
    @Override
    public int hashCode() {
        return Objects.hash(v);
    }

}
