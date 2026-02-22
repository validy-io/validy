package io.validy.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration for Validy's Spring Boot integration.
 *
 * <p>This does exactly two things:
 * <ol>
 *   <li>Registers a {@link ValidyValidator} bean if a {@link ValidatorRegistry} bean exists.</li>
 *   <li>Plugs that validator into Spring MVC via {@link WebMvcConfigurer#getValidator()}.</li>
 * </ol>
 *
 * <p>Nothing is activated unless <em>you</em> declare a {@code ValidatorRegistry} bean.
 * This configuration is purely additive and never replaces beans you've defined yourself.
 *
 * <p>Activated via {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.
 */
@AutoConfiguration
@ConditionalOnBean(ValidatorRegistry.class)
public class ValidyAutoConfiguration {

    /**
     * The core adapter. Only created when a {@link ValidatorRegistry} is present
     * and no custom {@link ValidyValidator} bean already exists.
     */
    @Bean
    @ConditionalOnMissingBean
    public ValidyValidator validyValidator(ValidatorRegistry registry) {
        return new ValidyValidator(registry);
    }

    /**
     * Plugs Validy into Spring MVC's validator chain so that {@code @Valid} on
     * {@code @RequestBody} parameters automatically triggers validation.
     *
     * <p>Returning a non-null value here replaces Spring's default
     * {@code LocalValidatorFactoryBean} (Hibernate Validator). If you want
     * <em>both</em> to run, return a {@link org.springframework.validation.SmartValidator}
     * that delegates to both — see the README for an example.
     */
    @Bean
    @ConditionalOnMissingBean(WebMvcConfigurer.class)
    public WebMvcConfigurer validyMvcConfigurer(ValidyValidator validyValidator) {
        return new WebMvcConfigurer() {
            @Override
            public Validator getValidator() {
                return validyValidator;
            }
        };
    }
}