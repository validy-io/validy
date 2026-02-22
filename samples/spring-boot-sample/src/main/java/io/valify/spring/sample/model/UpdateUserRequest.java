package io.valify.spring.sample.model;

public record UpdateUserRequest(
        String name,
        String email
) {}