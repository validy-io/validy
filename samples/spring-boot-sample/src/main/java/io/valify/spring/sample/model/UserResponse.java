package io.valify.spring.sample.model;

import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        int age,
        Address address,
        List<String> roles
) {}