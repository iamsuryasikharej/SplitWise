package com.example.demo.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PerUserBalance implements Comparable<PerUserBalance> {
    int bal;
    User user;

    @Override
    public int compareTo(PerUserBalance o) {
        return Integer.compare(this.bal, o.bal);
    }
}
