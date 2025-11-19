package com.example.demo.models;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@Data
public class Expense {
    // private final Map<User,Balance> userBalanceMap=new HashMap<>();
    private final BalanceMap balanceMap;
    private final String title;
    private final String expenseDescrtiption;
    private final String imageURL;
}
