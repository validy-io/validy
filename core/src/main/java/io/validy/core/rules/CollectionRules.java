package io.validy.core.rules;

import io.validy.core.Rule;
import io.validy.core.result.ValidationResult;

import java.util.Collection;
import java.util.Objects;

/**
 * Built-in rules for {@link Collection}s and general objects.
 */
public final class CollectionRules {

    private CollectionRules() {}

    // ── Collections ───────────────────────────────────────────────────────────

    public static <C extends Collection<?>> Rule<C> notEmpty() {
        return value -> (value != null && !value.isEmpty())
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must not be empty");
    }

    public static <C extends Collection<?>> Rule<C> minSize(int min) {
        return value -> (value != null && value.size() >= min)
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must have at least %d element(s)".formatted(min));
    }

    public static <C extends Collection<?>> Rule<C> maxSize(int max) {
        return value -> (value != null && value.size() <= max)
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must have at most %d element(s)".formatted(max));
    }

    // ── General object ────────────────────────────────────────────────────────

    public static <T> Rule<T> notNull() {
        return value -> value != null
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must not be null");
    }

    public static <T> Rule<T> equalTo(T expected) {
        return value -> Objects.equals(value, expected)
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must equal %s".formatted(expected));
    }

    public static <T> Rule<T> satisfies(java.util.function.Predicate<T> predicate, String message) {
        return value -> predicate.test(value)
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", message);
    }
}