package io.validy.spring.sample.model;

public record UpdateUserRequest(
        String name,
        String email
) {}