package io.validy.spring.sample.service;

import io.validy.spring.sample.model.CreateUserRequest;
import io.validy.spring.sample.model.UpdateUserRequest;
import io.validy.core.Validator;
import io.validy.core.result.ValidationResult;
import io.validy.spring.sample.model.UserResponse;
import io.validy.spring.sample.validators.UserValidators;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User service demonstrating manual (imperative) validation in the service layer.
 *
 * This is independent of the HTTP layer — useful for batch jobs, CLI tools,
 * or anywhere you need to validate without a Spring MVC request cycle.
 */
@Service
public class UserService {

    // ── In-memory store ───────────────────────────────────────────────────────

    private final Map<UUID, UserResponse> store = new ConcurrentHashMap<>();

    // ── Validators (plain objects — no Spring needed to construct them) ───────

    private final Validator<CreateUserRequest> createValidator = UserValidators.createUser();
    private final Validator<UpdateUserRequest> updateValidator = UserValidators.updateUser();

    // ── Service methods ───────────────────────────────────────────────────────

    /**
     * Validates and creates a user.
     * Throws {@link ValidationException} if the request is invalid.
     */
    public UserResponse create(CreateUserRequest request) {
        // Manual validation — explicit, no annotation magic
        switch (createValidator.validate(request)) {
            case ValidationResult.Valid   v  -> { /* proceed */ }
            case ValidationResult.Invalid iv ->
                throw new ValidationException(iv.errors());
        }

        var user = new UserResponse(
            UUID.randomUUID(),
            request.name(),
            request.email(),
            request.age(),
            request.address(),
            request.roles()
        );

        store.put(user.id(), user);
        return user;
    }

    /**
     * Validates and updates a user.
     */
    public UserResponse update(UUID id, UpdateUserRequest request) {
        switch (updateValidator.validate(request)) {
            case ValidationResult.Valid   v  -> { /* proceed */ }
            case ValidationResult.Invalid iv ->
                throw new ValidationException(iv.errors());
        }

        var existing = findById(id);
        var updated  = new UserResponse(
            existing.id(),
            request.name(),
            request.email(),
            existing.age(),
            existing.address(),
            existing.roles()
        );

        store.put(id, updated);
        return updated;
    }

    public UserResponse findById(UUID id) {
        var user = store.get(id);
        if (user == null) throw new NoSuchElementException("User not found: " + id);
        return user;
    }

    public List<UserResponse> findAll() {
        return List.copyOf(store.values());
    }

    public void delete(UUID id) {
        if (store.remove(id) == null) throw new NoSuchElementException("User not found: " + id);
    }
}
