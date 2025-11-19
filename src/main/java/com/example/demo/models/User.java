package com.example.demo.models;

import lombok.*;

import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor

public class User {
    private final String id, firstName, LastName, bio, imageURL;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, LastName, bio, imageURL);
    }

    @Override
    public String toString() {
        return this.firstName;
    }

}
