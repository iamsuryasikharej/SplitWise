package com.example.demo.controller;

import com.example.demo.models.Expense;
import com.example.demo.service.ExpenseService;
import com.example.demo.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RequiredArgsConstructor
@RestController
public class TestController {

    private final GroupService groupService;

    @GetMapping("/getGroupExpenses")
    public Expense getGroupExpenses() {
        return groupService.getBalances("123", "12345");

    }

    // @GetMapping("/getGroupExpenses")
    // public Expense getPaymentGrph(String groupId,String userId)
    // {
    // return groupService.getBalances("123","12345");
    //
    // }
}
