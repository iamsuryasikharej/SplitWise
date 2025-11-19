package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@Getter
public class Balance {
    private final String currency = "inr";
    private BigDecimal amount;

}
