package io.valify.core.result;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents the outcome of a validation run.
 * Uses Java 21+ sealed types + records for exhaustive pattern matching.
 */
public sealed interface ValidationResult permits ValidationResult.Valid, ValidationResult.Invalid {

    record Valid() implements ValidationResult {}

    record Invalid(List<ValidationError> errors) implements ValidationResult {
        public Invalid {
            errors = List.copyOf(errors);
        }
        public String summary() {
            return errors.stream()
                    .map(e -> "• [%s] %s".formatted(e.field(), e.message()))
                    .reduce("Validation failed:\n", (a, b) -> a + "\n" + b);
        }
    }

    // ── Factories ──────────────────────────────────────────────────────────────

    static ValidationResult valid() {
        return new Valid();
    }

    static ValidationResult invalid(List<ValidationError> errors) {
        return new Invalid(errors);
    }

    static ValidationResult invalid(String field, String message) {
        return new Invalid(List.of(new ValidationError(field, message)));
    }

    // ── Combinators ───────────────────────────────────────────────────────────

    default ValidationResult and(ValidationResult other) {
        return switch (this) {
            case Valid() -> other;
            case Invalid(var errs) -> switch (other) {
                case Valid() -> this;
                case Invalid(var otherErrs) -> {
                    var combined = new java.util.ArrayList<>(errs);
                    combined.addAll(otherErrs);
                    yield new Invalid(combined);
                }
            };
        };
    }

    default boolean isValid() {
        return this instanceof Valid;
    }

    default void ifInvalid(Consumer<Invalid> consumer) {
        if (this instanceof Invalid i) consumer.accept(i);
    }

    default void ifValid(Runnable action) {
        if (this instanceof Valid) action.run();
    }
}