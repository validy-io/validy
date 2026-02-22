package io.valify.core;

import io.valify.core.result.ValidationError;
import io.valify.core.result.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Fluent builder that composes field-level and object-level rules
 * into a single {@link Rule} for type {@code T}.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * var validator = Validator.<User>of()
 *     .field("name",    User::name,    StringRules.notBlank(), StringRules.maxLength(100))
 *     .field("email",   User::email,   StringRules.notBlank(), StringRules.email())
 *     .field("age",     User::age,     NumberRules.min(0),     NumberRules.max(150))
 *     .rule(u -> u.password().equals(u.confirm())
 *         ? ValidationResult.valid()
 *         : ValidationResult.invalid("confirm", "Passwords do not match"))
 *     .build();
 * }</pre>
 *
 * @param <T> the object type to validate
 */
public final class Validator<T> implements Rule<T> {

    private final List<Rule<T>> rules;

    private Validator(List<Rule<T>> rules) {
        this.rules = List.copyOf(rules);
    }

    // ── DSL entry point ───────────────────────────────────────────────────────

    public static <T> Builder<T> of() {
        return new Builder<>();
    }

    // ── Rule implementation ───────────────────────────────────────────────────

    @Override
    public ValidationResult validate(T value) {
        return rules.stream()
                .map(r -> r.validate(value))
                .reduce(ValidationResult.valid(), ValidationResult::and);
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder<T> {

        private final List<Rule<T>> rules = new ArrayList<>();

        /**
         * Validates a field extracted from {@code T} against one or more rules.
         *
         * @param name      field name (used in error messages)
         * @param extractor accessor function
         * @param fieldRules rules applied to the extracted value
         */
        @SafeVarargs
        public final <F> Builder<T> field(String name, Function<T, F> extractor, Rule<F>... fieldRules) {
            for (var rule : fieldRules) {
                rules.add(obj -> rule.as(name).validate(extractor.apply(obj)));
            }
            return this;
        }

        /**
         * Adds a raw object-level rule (cross-field validation, invariants, etc.).
         */
        public Builder<T> rule(Rule<T> rule) {
            rules.add(rule);
            return this;
        }

        /**
         * Adds a conditional rule — only evaluated when the predicate holds.
         */
        public Builder<T> when(java.util.function.Predicate<T> condition, Rule<T> rule) {
            rules.add(obj -> condition.test(obj) ? rule.validate(obj) : ValidationResult.valid());
            return this;
        }

        /**
         * Embeds a nested validator for a child object.
         */
        public <C> Builder<T> nested(String prefix, Function<T, C> extractor, Rule<C> nestedRule) {
            rules.add(obj -> {
                var child = extractor.apply(obj);
                return switch (nestedRule.validate(child)) {
                    case ValidationResult.Valid v -> v;
                    case ValidationResult.Invalid(var errors) ->
                            ValidationResult.invalid(
                                    errors.stream()
                                            .map(e -> new ValidationError(prefix + "." + e.field(), e.message()))
                                            .toList()
                            );
                };
            });
            return this;
        }

        public Validator<T> build() {
            return new Validator<>(rules);
        }
    }
}