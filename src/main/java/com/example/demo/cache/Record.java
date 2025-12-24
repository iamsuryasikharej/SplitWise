package com.example.demo.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Objects;

@Builder
@AllArgsConstructor
@Data
public class Record<V> {
    final V v;
    final Instant timeStamp;
    final Instant loadTime;
    AccessDetails accessDetails;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Record<?> rec = (Record<?>) obj;
        return Objects.equals(this.v, rec.v);
    }

    public Record(Record<V> r) {
        this.v = r.v;
        this.timeStamp = r.timeStamp;
        this.loadTime = r.loadTime;
        this.accessDetails = new AccessDetails(r.accessDetails.accessCount + 1, r.accessDetails.accessedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(v);
    }

    @Override
    public String toString() {
        return v.toString();
    }

}
