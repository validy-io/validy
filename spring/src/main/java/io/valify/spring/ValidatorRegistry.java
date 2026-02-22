package io.valify.spring;

import io.valify.core.Rule;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A simple registry that maps a class to its {@link Rule}.
 *
 * <p>You populate this yourself in a {@code @Configuration} class — no scanning,
 * no reflection, no magic. You decide exactly what gets validated and how.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * @Bean
 * ValidatorRegistry validatorRegistry() {
 *     return ValidatorRegistry.builder()
 *         .register(CreateUserRequest.class, UserValidators.createUser())
 *         .register(UpdateUserRequest.class, UserValidators.updateUser())
 *         .register(PaymentRequest.class,    PaymentValidators.payment())
 *         .build();
 * }
 * }</pre>
 */
public final class ValidatorRegistry {

    private final Map<Class<?>, Rule<?>> validators;

    private ValidatorRegistry(Map<Class<?>, Rule<?>> validators) {
        this.validators = Map.copyOf(validators);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<Rule<T>> findFor(Class<T> type) {
        return Optional.ofNullable((Rule<T>) validators.get(type));
    }

    public boolean supports(Class<?> type) {
        return validators.containsKey(type);
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Map<Class<?>, Rule<?>> validators = new HashMap<>();

        public <T> Builder register(Class<T> type, Rule<T> rule) {
            validators.put(type, rule);
            return this;
        }

        public ValidatorRegistry build() {
            return new ValidatorRegistry(validators);
        }
    }
}