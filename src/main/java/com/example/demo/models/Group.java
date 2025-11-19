package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class Group {
    private final String id, name, description;
    private final List<User> users;
    private final List<Expense> expenses;

    public Group(Group g)
    {
        this.id=g.id;
        this.name=g.name;
        this.description=g.description;
        this.users=g.getUsers();
        this.expenses=g.getExpenses();
    }

}
