package io.valify.spring.sample.config;
import io.valify.spring.ValidatorRegistry;
import io.valify.spring.ValidationExceptionHandler;
import io.valify.spring.sample.model.CreateUserRequest;
import io.valify.spring.sample.model.UpdateUserRequest;
import io.valify.spring.sample.validators.UserValidators;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Single configuration class that wires all Valify validators into Spring.
 *
 * Add a new validator here when you add a new request type — that's the
 * only file you ever need to touch.
 */
@Configuration
public class ValifyConfig {

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
