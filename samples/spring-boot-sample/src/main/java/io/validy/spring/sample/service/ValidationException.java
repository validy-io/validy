package io.validy.spring.sample.service;

import io.validy.core.result.ValidationError;

import java.util.List;

/**
 * Thrown by the service layer when validation fails outside the HTTP request cycle.
 * Carries the full list of errors so callers can decide how to present them.
 */
public class ValidationException extends RuntimeException {

    private final List<ValidationError> errors;

    public ValidationException(List<ValidationError> errors) {
        super("Validation failed: " + errors.stream()
                .map(e -> "[%s] %s".formatted(e.field(), e.message()))
                .reduce("", (a, b) -> a.isBlank() ? b : a + ", " + b));
        this.errors = List.copyOf(errors);
    }

    public List<ValidationError> errors() {
        return errors;
    }
}