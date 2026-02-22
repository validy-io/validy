package io.validy.core.rules;

import io.validy.core.Rule;
import io.validy.core.result.ValidationError;
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

    // ── Element-level validation ──────────────────────────────────────────────

    /**
     * Validates every element in a collection against the given rule.
     *
     * <p>Errors are collected from all elements — validation does not stop
     * at the first failure. Each error field is prefixed with the element's
     * index, e.g. {@code "[0]"}, {@code "[1].email"}.
     *
     * <pre>{@code
     * // Simple element rule
     * Rule<List<String>> allNotBlank = eachElement(notBlank());
     *
     * // With a full object validator per element
     * Rule<List<Address>> allValidAddresses = eachElement(addressValidator);
     * }</pre>
     *
     * @param elementRule the rule applied to each element
     * @param <E>         the element type
     * @param <C>         the collection type
     */
    public static <E, C extends Collection<E>> Rule<C> eachElement(Rule<E> elementRule) {
        return collection -> {
            if (collection == null) {
                return ValidationResult.invalid("$", "must not be null");
            }

            var errors = new java.util.ArrayList<ValidationError>();
            int index  = 0;

            for (E element : collection) {
                final int i = index++;
                switch (elementRule.validate(element)) {
                    case ValidationResult.Valid   v  -> { /* ok */ }
                    case ValidationResult.Invalid iv -> iv.errors().forEach(e -> {
                        // "$" sentinel → bare index "[0]"
                        // nested field  → "[0].field"
                        String field = "$".equals(e.field())
                                ? "[%d]".formatted(i)
                                : "[%d].%s".formatted(i, e.field());
                        errors.add(new ValidationError(field, e.message()));
                    });
                }
            }

            return errors.isEmpty()
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(errors);
        };
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