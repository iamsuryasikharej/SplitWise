package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Builder
@AllArgsConstructor
@Getter
public class BalanceMap {
    private Map<User, Balance> userBalanceMap = new HashMap<>();
}
