package io.valify.spring.sample.model;

import java.util.List;

public record CreateUserRequest(
        String name,
        String email,
        int age,
        String password,
        String confirmPassword,
        Address address,
        List<String> roles
) {}

