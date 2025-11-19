package com.example.demo.service;

import com.example.demo.models.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    public Expense getGroupExpenses(String groupId, Group g) {

        if (!g.getId().equals(groupId)) {
            throw new RuntimeException("Unauthorized");
        }
        List<User> user = g.getUsers();

        List<Expense> expenses = g.getExpenses();

        // Map<User, BigDecimal> finalMap = balanceMaps.stream()
        // .flatMap(m -> m.entrySet().stream()) // flatten all maps
        // .collect(Collectors.toMap(
        // Map.Entry::getKey, // group by User
        // e -> e.getValue().getAmount(), // starting value
        // BigDecimal::add // merge values when duplicate User is found
        // ));
        // finalMap.forEach((x,t)-> System.out.println(x.getFirstName()+"-->"+t.toString()));

        Map<User, Balance> finalBal = new HashMap<>();
        expenses.stream().map(expense1 -> expense1.getBalanceMap().getUserBalanceMap()).toList()
                .forEach(x -> x.forEach((y, j) -> {
                    Balance i = finalBal.get(y) != null
                            ? finalBal.put(y, Balance.builder()
                                    .amount(new BigDecimal(
                                            finalBal.get(y).getAmount().intValue() + j.getAmount().intValue()))
                                    .build())
                            : finalBal.put(y,
                                    Balance.builder().amount(new BigDecimal(j.getAmount().intValue())).build());
                }));

        finalBal.forEach((x, y) -> System.out.println(x.getFirstName() + "<--->" + y.getAmount()));


        return Expense.builder().balanceMap(new BalanceMap(finalBal)).build();

    }

    public PaymentGraph getPaymentGraph(Expense resultExpense) {
        return createHeaps(resultExpense);
    }

    private PaymentGraph createHeaps(Expense resultExpense) {
        PriorityQueue<PerUserBalance> maxHeap = new PriorityQueue<>((a, b) -> b.getBal() - a.getBal());
        PriorityQueue<PerUserBalance> minHeap = new PriorityQueue<>();
        resultExpense.getBalanceMap().getUserBalanceMap().forEach((l, m) -> {
            if (m.getAmount().intValue() < 0) {
                minHeap.add(PerUserBalance.builder().user(l).bal(m.getAmount().intValue()).build());
            } else if (m.getAmount().intValue() > 0) {
                maxHeap.add(PerUserBalance.builder().user(l).bal(m.getAmount().intValue()).build());
            }

        });
        return createPaymentGraph(minHeap, maxHeap);
    }

    private PaymentGraph createPaymentGraph(PriorityQueue<PerUserBalance> minHeap, PriorityQueue<PerUserBalance> maxHeap) {
        StringBuilder sb=new StringBuilder();

        Map<User,BalanceMap> mp=new HashMap<>();
        while (!minHeap.isEmpty()) {
            PerUserBalance min = minHeap.poll();
            PerUserBalance max = maxHeap.poll();
            int bal = min.getBal() + max.getBal();
            System.out.println(min.getUser().getFirstName() + "paid " + max.getUser().getFirstName() + "money-->"
                    + (Math.max(Math.abs(min.getBal()), Math.abs(max.getBal())) - Math.abs(bal)));
            sb.append(min.getUser().getFirstName() + "paid " + max.getUser().getFirstName() + "money-->"
                    + (Math.max(Math.abs(min.getBal()), Math.abs(max.getBal())) - Math.abs(bal)));

            Object o = mp.get(min.getUser()) == null ?
                    mp.put(min.getUser(), BalanceMap.builder()
                            .userBalanceMap(new HashMap<>(
                                            Map.of(max.getUser(), Balance.builder().amount(new BigDecimal((Math.max(Math.abs(min.getBal()), Math.abs(max.getBal())) - Math.abs(bal)))).build())
                                    )
                                    ).build())
                    : mp.get(min.getUser()).getUserBalanceMap().put(max.getUser(), Balance.builder().amount(new BigDecimal((Math.max(Math.abs(min.getBal()), Math.abs(max.getBal())) - Math.abs(bal)))).build());


            boolean b = bal < 0 ? minHeap.add(PerUserBalance.builder().user(min.getUser()).bal(bal).build())
                    : maxHeap.add(PerUserBalance.builder().user(max.getUser()).bal(bal).build());
        }
        return PaymentGraph.builder().graph(mp).build();

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
