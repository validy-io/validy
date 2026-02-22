package io.validy.core;

import io.validy.core.result.ValidationError;
import io.validy.core.result.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Fluent builder that assembles field-level, cross-field, conditional,
 * and nested rules — each optionally scoped to one or more {@link ValidationGroup}s.
 *
 * <h2>Group semantics</h2>
 * <ul>
 *   <li>Rules with <b>no group</b> (or group {@code Default}) run on every call.</li>
 *   <li>Rules with an <b>explicit group</b> only run when that group is requested.</li>
 *   <li>{@code validate(value)} — runs Default rules only.</li>
 *   <li>{@code validate(value, OnCreate.class)} — runs Default + OnCreate rules.</li>
 *   <li>{@code validate(value, OnCreate.class, OnUpdate.class)} — runs Default + both.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * interface OnCreate extends ValidationGroup {}
 * interface OnUpdate extends ValidationGroup {}
 *
 * var validator = validator(User.class)
 *     .field("name",     User::name,     notBlank(), maxLength(100))          // Default
 *     .field("email",    User::email,    notBlank(), email())                 // Default
 *     .field("password", User::password, strongPassword()).groups(OnCreate)   // OnCreate only
 *     .field("id",       User::id,       notNull())      .groups(OnUpdate)   // OnUpdate only
 *     .build();
 *
 * validator.validate(user);                   // name + email only
 * validator.validate(user, OnCreate.class);  // name + email + password
 * validator.validate(user, OnUpdate.class);  // name + email + id
 * }</pre>
 */
public final class Validator<T> implements Rule<T> {

    private final List<GroupedRule<T>> groupedRules;

    private Validator(List<GroupedRule<T>> groupedRules) {
        this.groupedRules = List.copyOf(groupedRules);
    }

    public static <T> Builder<T> of() {
        return new Builder<>();
    }

    // ── Rule implementation ───────────────────────────────────────────────────

    /**
     * Runs all Default-group rules (no group filter).
     */
    @Override
    public ValidationResult validate(T value) {
        return validate(value, ValidationGroup.Default.class);
    }

    /**
     * Runs Default rules plus any rules belonging to the requested groups.
     */
    @SafeVarargs
    public final ValidationResult validate(T value, Class<? extends ValidationGroup>... groups) {
        var activeGroups = Set.of(groups);
        return groupedRules.stream()
                .filter(gr -> gr.isActiveFor(activeGroups))
                .map(gr -> gr.rule().validate(value))
                .reduce(ValidationResult.valid(), ValidationResult::and);
    }

    // ── Internal: pairs a rule with its groups ────────────────────────────────

    private record GroupedRule<T>(
            Rule<T> rule,
            Set<Class<? extends ValidationGroup>> groups
    ) {
        boolean isActiveFor(Set<Class<? extends ValidationGroup>> activeGroups) {
            // No explicit groups = Default = always runs
            if (groups.isEmpty() || groups.contains(ValidationGroup.Default.class)) return true;
            return groups.stream().anyMatch(activeGroups::contains);
        }
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder<T> {

        final List<GroupedRule<T>> rules = new ArrayList<>();

        @SafeVarargs
        public final <F> FieldStep<T, F> field(String name, Function<T, F> extractor, Rule<F>... fieldRules) {
            return new FieldStep<>(this, name, extractor, List.of(fieldRules));
        }

        public RuleStep<T> require(String name, Function<T, ?> extractor) {
            return rule(obj -> extractor.apply(obj) != null
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(name, "is required"));
        }

        public RuleStep<T> rule(Rule<T> rule) {
            return new RuleStep<>(this, rule);
        }

        public RuleStep<T> when(Predicate<T> condition, Rule<T> rule) {
            return rule(obj -> condition.test(obj) ? rule.validate(obj) : ValidationResult.valid());
        }

        public <C> RuleStep<T> nested(String prefix, Function<T, C> extractor, Rule<C> nestedRule) {
            return rule(obj -> switch (nestedRule.validate(extractor.apply(obj))) {
                case ValidationResult.Valid   v -> v;
                case ValidationResult.Invalid i ->
                        ValidationResult.invalid(
                                i.errors().stream()
                                        .map(e -> new ValidationError(prefix + "." + e.field(), e.message()))
                                        .toList()
                        );
            });
        }

        @SafeVarargs
        final Builder<T> addRule(Rule<T> rule, Class<? extends ValidationGroup>... groups) {
            this.rules.add(new GroupedRule<>(rule, Set.of(groups)));
            return this;
        }

        public Validator<T> build() {
            return new Validator<>(rules);
        }
    }

    // ── Step builders — allow .groups() chaining after each builder call ──────

    /**
     * Returned by {@code .field(...)} — call {@code .groups(...)} to scope,
     * or continue chaining other builder methods directly.
     */
    public static final class FieldStep<T, F> {

        private final Builder<T>     builder;
        private final String         name;
        private final Function<T, F> extractor;
        private final List<Rule<F>>  fieldRules;

        FieldStep(Builder<T> builder, String name, Function<T, F> extractor, List<Rule<F>> fieldRules) {
            this.builder    = builder;
            this.name       = name;
            this.extractor  = extractor;
            this.fieldRules = fieldRules;
            commit(); // register with Default group immediately
        }

        /** Scopes these field rules to the given groups, replacing the Default registration. */
        @SafeVarargs
        public final Builder<T> groups(Class<? extends ValidationGroup>... groups) {
            removeLast(fieldRules.size());
            for (var fr : fieldRules) {
                builder.addRule(obj -> fr.as(name).validate(extractor.apply(obj)), groups);
            }
            return builder;
        }

        // ── Delegate so chaining continues without calling .groups() ──────────
        @SafeVarargs
        public final <F2> FieldStep<T, F2> field(String n, Function<T, F2> e, Rule<F2>... fr) { return builder.field(n, e, fr); }
        public RuleStep<T> rule(Rule<T> r)                             { return builder.rule(r); }
        public RuleStep<T> when(Predicate<T> c, Rule<T> r)            { return builder.when(c, r); }
        public RuleStep<T> require(String n, Function<T, ?> e)        { return builder.require(n, e); }
        public <C> RuleStep<T> nested(String p, Function<T,C> e, Rule<C> nr) { return builder.nested(p, e, nr); }
        public Validator<T> build()                                    { return builder.build(); }

        private void commit() {
            for (var fr : fieldRules) {
                builder.addRule(obj -> fr.as(name).validate(extractor.apply(obj)));
            }
        }

        private void removeLast(int count) {
            for (int i = 0; i < count; i++) {
                builder.rules.remove(builder.rules.size() - 1);
            }
        }
    }

    /**
     * Returned by {@code .rule()}, {@code .when()}, {@code .require()}, {@code .nested()} —
     * call {@code .groups(...)} to scope, or continue chaining.
     */
    public static final class RuleStep<T> {

        private final Builder<T> builder;
        private final Rule<T>    rule;

        RuleStep(Builder<T> builder, Rule<T> rule) {
            this.builder = builder;
            this.rule    = rule;
            builder.addRule(rule); // register with Default group immediately
        }

        /** Scopes this rule to the given groups, replacing the Default registration. */
        @SafeVarargs
        public final Builder<T> groups(Class<? extends ValidationGroup>... groups) {
            builder.rules.remove(builder.rules.size() - 1);
            builder.addRule(rule, groups);
            return builder;
        }

        // ── Delegate so chaining continues without calling .groups() ──────────
        @SafeVarargs
        public final <F> FieldStep<T, F> field(String n, Function<T, F> e, Rule<F>... fr) { return builder.field(n, e, fr); }
        public RuleStep<T> rule(Rule<T> r)                             { return builder.rule(r); }
        public RuleStep<T> when(Predicate<T> c, Rule<T> r)            { return builder.when(c, r); }
        public RuleStep<T> require(String n, Function<T, ?> e)        { return builder.require(n, e); }
        public <C> RuleStep<T> nested(String p, Function<T,C> e, Rule<C> nr) { return builder.nested(p, e, nr); }
        public Validator<T> build()                                    { return builder.build(); }
    }
}