package com.example.demo.cache;

import lombok.AllArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
public class AccessDetails {

    int accessCount;
    Instant accessedAt;
}
