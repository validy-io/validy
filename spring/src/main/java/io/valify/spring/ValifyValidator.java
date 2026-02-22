package io.valify.spring;

import io.valify.core.result.ValidationResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Adapts Valify's {@link io.valify.core.Rule} to Spring's {@link Validator} SPI.
 *
 * <p>Once this bean is registered, Spring MVC will automatically invoke it for
 * any controller parameter annotated with {@code @Valid} or {@code @Validated},
 * as long as the target type is registered in the {@link ValidatorRegistry}.
 *
 * <p>No proxies, no AOP, no annotation scanning — Spring calls this directly
 * via its standard {@code HandlerMethodArgumentResolver} pipeline.
 */
public final class ValifyValidator implements Validator {

    private final ValidatorRegistry registry;

    public ValifyValidator(ValidatorRegistry registry) {
        this.registry = registry;
    }

    /**
     * Spring calls this to decide whether to use this validator for a given type.
     * We only claim types that are explicitly registered — no surprises.
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return registry.supports(clazz);
    }

    /**
     * Spring calls this with the target object and an {@link Errors} collector.
     * We run the Valify rule and translate errors into Spring's format.
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void validate(Object target, Errors errors) {
        Rule rule = (Rule) registry.findFor(target.getClass()).orElse(null);
        if (rule == null) return;

        switch (rule.validate(target)) {
            case ValidationResult.Valid   v  -> { /* nothing to do */ }
            case ValidationResult.Invalid iv -> iv.errors().forEach(e -> {
                // "$" is our sentinel for object-level (non-field) errors
                if ("$".equals(e.field())) {
                    errors.reject("validation.error", e.message());
                } else {
                    errors.rejectValue(e.field(), "validation.error", e.message());
                }
            });
        }
    }
}

// ── Inner import alias to keep this file self-contained ──────────────────────
// (avoids ambiguity between io.valify.core.Rule and anything Spring might expose)
interface Rule<T> extends io.valify.core.Rule<T> {}