package io.validy.core;

import io.validy.core.result.ValidationResult;
import io.validy.core.rules.CollectionRules;
import io.validy.core.rules.InstantRules;
import io.validy.core.rules.NumberRules;
import io.validy.core.rules.StringRules;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Central façade — a single {@code import static io.validy.core.Validy.*} gives access
 * to the entire DSL.
 *
 * <h2>Quick-start</h2>
 * <pre>{@code
 * import static io.validy.core.Validy.*;
 *
 * var userValidator = validator(User.class)
 *     .field("name",  User::name,  notBlank(), maxLength(100))
 *     .field("email", User::email, notBlank(), email())
 *     .field("age",   User::age,   min(0),     max(150))
 *     .build();
 *
 * switch (userValidator.validate(user)) {
 *     case Valid()         -> System.out.println("All good!");
 *     case Invalid(var e)  -> System.err.println(e.summary());
 * }
 * }</pre>
 */
public final class Validy {

    private Validy() {
    }

    // ── Validator factory ─────────────────────────────────────────────────────

    /**
     * Creates a new {@link Validator.Builder} for type {@code T}.
     */
    public static <T> Validator.Builder<T> validator(Class<T> type) {
        return Validator.of();
    }

    /**
     * Inline rule from a predicate — the quickest way to define a custom rule.
     */
    public static <T> Rule<T> rule(Predicate<T> predicate, String errorMessage) {
        return CollectionRules.satisfies(predicate, errorMessage);
    }


    /**
     * Runs a validator for the given groups in addition to Default rules.
     * Shorthand for {@code myValidator.validate(value, groups)}.
     *
     * <pre>{@code
     * validate(userValidator, user, OnCreate.class);
     * validate(userValidator, user, OnCreate.class, OnUpdate.class);
     * }</pre>
     */
    @SafeVarargs
    public static <T> ValidationResult validate(
            Validator<T> validator,
            T value,
            Class<? extends ValidationGroup>... groups) {
        return validator.validate(value, groups);
    }


    // ── String rules ──────────────────────────────────────────────────────────

    public static Rule<String> notBlank() {
        return StringRules.notBlank();
    }

    public static Rule<String> notNull() {
        return StringRules.notNull();
    }

    public static Rule<String> minLength(int min) {
        return StringRules.minLength(min);
    }

    public static Rule<String> maxLength(int max) {
        return StringRules.maxLength(max);
    }

    public static Rule<String> length(int min, int max) {
        return StringRules.length(min, max);
    }

    public static Rule<String> email() {
        return StringRules.email();
    }

    public static Rule<String> url() {
        return StringRules.url();
    }

    public static Rule<String> uuid() {
        return StringRules.uuid();
    }

    public static Rule<String> numeric() {
        return StringRules.numeric();
    }

    public static Rule<String> matches(String regex) {
        return StringRules.matches(regex);
    }

    public static Rule<String> oneOf(String... values) {
        return StringRules.oneOf(values);
    }

    // ── Number rules ──────────────────────────────────────────────────────────

    public static <N extends Comparable<N>> Rule<N> min(N min) {
        return NumberRules.min(min);
    }

    public static <N extends Comparable<N>> Rule<N> max(N max) {
        return NumberRules.max(max);
    }

    public static <N extends Comparable<N>> Rule<N> between(N min, N max) {
        return NumberRules.between(min, max);
    }

    public static Rule<Integer> positive() {
        return NumberRules.positive();
    }

    public static Rule<Integer> nonNegative() {
        return NumberRules.nonNegative();
    }

    // ── Collection rules ──────────────────────────────────────────────────────

    public static <C extends Collection<?>> Rule<C> notEmpty() {
        return CollectionRules.notEmpty();
    }

    public static <C extends Collection<?>> Rule<C> minSize(int min) {
        return CollectionRules.minSize(min);
    }

    public static <C extends Collection<?>> Rule<C> maxSize(int max) {
        return CollectionRules.maxSize(max);
    }

    /**
     * Validates every element in a collection, prefixing error fields with the
     * element index: {@code [0]}, {@code [1].email}, etc.
     *
     * <pre>{@code
     * // Validate each tag string
     * .field("tags", User::tags, eachElement(notBlank()))
     *
     * // Validate each Address with its own validator
     * .field("addresses", User::addresses, eachElement(addressValidator))
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public static <E, C extends Collection<E>> Rule<C> eachElement(Rule<E> elementRule) {
        return CollectionRules.eachElement(elementRule);
    }

    // ── Object rules ──────────────────────────────────────────────────────────

    public static <T> Rule<T> notNullObj() {
        return CollectionRules.notNull();
    }

    // ── Instant / temporal rules ──────────────────────────────────────────────

    public static Rule<java.time.Instant> future() {
        return InstantRules.future();
    }

    public static Rule<java.time.Instant> futureOrPresent() {
        return InstantRules.futureOrPresent();
    }

    public static Rule<java.time.Instant> past() {
        return InstantRules.past();
    }

    public static Rule<java.time.Instant> after(java.time.Instant ref) {
        return InstantRules.after(ref);
    }

    public static Rule<java.time.Instant> before(java.time.Instant ref) {
        return InstantRules.before(ref);
    }
}