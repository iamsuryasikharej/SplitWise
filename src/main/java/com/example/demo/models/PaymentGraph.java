package com.example.demo.models;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class PaymentGraph {
    private final Map<User, BalanceMap> graph;
}
