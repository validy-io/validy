package io.validy.core.rules;

import io.validy.core.Rule;
import io.validy.core.result.ValidationResult;

import java.time.Instant;

/**
 * Built-in rules for {@link Instant} values.
 */
public final class InstantRules {

    private InstantRules() {}

    public static Rule<Instant> notNull() {
        return value -> value != null
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must not be null");
    }

    /** Value must be strictly in the future (after now at time of validation). */
    public static Rule<Instant> future() {
        return value -> (value != null && value.isAfter(Instant.now()))
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must be in the future");
    }

    /** Value must be in the future or equal to now. */
    public static Rule<Instant> futureOrPresent() {
        return value -> (value != null && !value.isBefore(Instant.now()))
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must be in the present or future");
    }

    /** Value must be strictly in the past. */
    public static Rule<Instant> past() {
        return value -> (value != null && value.isBefore(Instant.now()))
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must be in the past");
    }

    /** Value must be after a fixed reference instant. */
    public static Rule<Instant> after(Instant reference) {
        return value -> (value != null && value.isAfter(reference))
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must be after %s".formatted(reference));
    }

    /** Value must be before a fixed reference instant. */
    public static Rule<Instant> before(Instant reference) {
        return value -> (value != null && value.isBefore(reference))
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must be before %s".formatted(reference));
    }
}
