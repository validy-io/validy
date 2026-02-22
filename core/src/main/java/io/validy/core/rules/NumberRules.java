package io.validy.core.rules;

import io.validy.core.Rule;
import io.validy.core.result.ValidationResult;

/**
 * Built-in rules for {@link Number} subclasses (Integer, Long, Double, BigDecimal, …).
 */
public final class NumberRules {

    private NumberRules() {}

    public static <N extends Comparable<N>> Rule<N> min(N minimum) {
        return value -> (value != null && value.compareTo(minimum) >= 0)
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must be ≥ %s".formatted(minimum));
    }

    public static <N extends Comparable<N>> Rule<N> max(N maximum) {
        return value -> (value != null && value.compareTo(maximum) <= 0)
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must be ≤ %s".formatted(maximum));
    }

    public static <N extends Comparable<N>> Rule<N> between(N min, N max) {
        return NumberRules.<N>min(min).and(max(max));
    }

    public static <N extends Comparable<N>> Rule<N> notNull() {
        return value -> value != null
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must not be null");
    }

    public static Rule<Integer> positive() {
        return min(1);
    }

    public static Rule<Integer> nonNegative() {
        return min(0);
    }

    public static Rule<Long> positiveLong() {
        return min(1L);
    }

    public static Rule<Double> positiveDouble() {
        return value -> (value != null && value > 0)
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must be positive");
    }
}