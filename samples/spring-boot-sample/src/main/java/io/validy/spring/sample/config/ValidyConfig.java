package io.validy.spring.sample.config;
import io.validy.spring.ValidatorRegistry;
import io.validy.spring.ValidationExceptionHandler;
import io.validy.spring.sample.model.CreateUserRequest;
import io.validy.spring.sample.model.UpdateUserRequest;
import io.validy.spring.sample.validators.UserValidators;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Single configuration class that wires all Validy validators into Spring.
 *
 * Add a new validator here when you add a new request type — that's the
 * only file you ever need to touch.
 */
@Configuration
public class ValidyConfig {

    @Bean
    public ValidatorRegistry validatorRegistry() {
        return ValidatorRegistry.builder()
                .register(CreateUserRequest.class, UserValidators.createUser())
                .register(UpdateUserRequest.class, UserValidators.updateUser())
                .build();
    }

    @Bean
    public ValidationExceptionHandler validationExceptionHandler() {
        return new ValidationExceptionHandler();
    }
}
