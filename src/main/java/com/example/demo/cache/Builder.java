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
}