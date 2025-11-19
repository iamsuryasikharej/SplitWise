package com.example.demo.service;

import com.example.demo.models.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroupService {
    @Autowired
    private ExpenseService expenseService;

    Map<String, Group> groupCache = new HashMap<>();

    BalanceMap map = BalanceMap.builder()
            .userBalanceMap(Map.of(getListOfDummyUsers().get(0), Balance.builder().amount(new BigDecimal("30")).build(), // a
                    getListOfDummyUsers().get(1), Balance.builder().amount(new BigDecimal("-30")).build(), // b
                    getListOfDummyUsers().get(2), Balance.builder().amount(new BigDecimal("-30")).build(), // c
                    getListOfDummyUsers().get(3), Balance.builder().amount(new BigDecimal("40")).build(), // d
                    getListOfDummyUsers().get(4), Balance.builder().amount(new BigDecimal("-50")).build(), // e
                    getListOfDummyUsers().get(5), Balance.builder().amount(new BigDecimal("20")).build()// f
            )).build();

    BalanceMap map1 = BalanceMap.builder()
            .userBalanceMap(Map.of(getListOfDummyUsers().get(0), Balance.builder().amount(new BigDecimal("60")).build(), // a
                    getListOfDummyUsers().get(1), Balance.builder().amount(new BigDecimal("-40")).build(), // b
                    getListOfDummyUsers().get(2), Balance.builder().amount(new BigDecimal("-10")).build(), // c
                    getListOfDummyUsers().get(3), Balance.builder().amount(new BigDecimal("40")).build(), // d
                    getListOfDummyUsers().get(4), Balance.builder().amount(new BigDecimal("-50")).build(), // e
                    getListOfDummyUsers().get(5), Balance.builder().amount(new BigDecimal("20")).build()// f
            )).build();

    Expense expense = Expense.builder().expenseDescrtiption("our").title("demo").balanceMap(map).imageURL("asdfghj")
            .expenseDescrtiption("test").build();

    Expense expense2 = Expense.builder().expenseDescrtiption("our").title("demo").balanceMap(map1).imageURL("asdfghj")
            .expenseDescrtiption("test").build();

    Group g = Group.builder().id("123").users(List.of()).expenses(List.of(expense, expense2))
            .users(getListOfDummyUsers()).name("GoaGroup").description("goaTrip").build();

    public PaymentGraph getGroupPaymentGraph(final String groupId, final String userId) {

        // Expense resultExpense = sumAllGroupExpenses(expensesList);
        // return expenseService.getPaymentGraph(resultExpense);
        return null;

    }

    private Expense sumAllGroupExpenses(List<Expense> expensesList) {
        return null;
    }

    public Expense getBalances(final String groupId, String userId) {
        groupCache.put("123", g);
        if (groupCache.get(groupId).getUsers().stream().noneMatch((x) -> x.getId() == userId)) {
            throw new IllegalArgumentException("User provided is not present in this group");
        }
        return expenseService.getGroupExpenses(groupId, g);
    }

    public List<User> getListOfDummyUsers() {
        return List.of(User.builder().bio("erfr").id("1234").imageURL("qqqqq").firstName("a").LastName("").build(),
                User.builder().bio("erfr").id("12345").imageURL("qqqqq").firstName("b").LastName("").build(),
                User.builder().bio("erfr").id("123456").imageURL("qqqqq").firstName("c").LastName("").build(),
                User.builder().bio("erfr").id("1234567").imageURL("qqqqq").firstName("d").LastName("").build(),
                User.builder().bio("erfr").id("1234568").imageURL("qqqqq").firstName("e").LastName("").build(),
                User.builder().bio("erfr").id("1234569").imageURL("qqqqq").firstName("f").LastName("").build());
    }
}
