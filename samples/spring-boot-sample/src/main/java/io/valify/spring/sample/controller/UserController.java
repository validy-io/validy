package io.valify.spring.sample.controller;


import io.valify.spring.sample.model.*;

import io.valify.spring.sample.service.UserService;
import io.valify.spring.sample.service.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * REST controller demonstrating two validation styles side by side:
 *
 * 1. @Validated on @RequestBody  — Spring MVC calls ValifyValidator automatically,
 *                                   returns 422 on failure via ValidationExceptionHandler.
 *
 * 2. Service-layer exception     — UserService validates manually and throws
 *                                   ValidationException, handled here explicitly.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ── @Validated approach — Spring calls Valify automatically ───────────────

    /**
     * POST /users
     *
     * @Validated triggers ValifyValidator before the method body runs.
     * On failure Spring throws MethodArgumentNotValidException,
     * which ValidationExceptionHandler converts to a 422 Problem Detail.
     */
    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody @Validated CreateUserRequest request) {
        var user = userService.create(request);
        return ResponseEntity
            .created(URI.create("/users/" + user.id()))
            .body(user);
    }

    /**
     * PUT /users/{id}
     *
     * Same pattern — @Validated handles validation before the method runs.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
        @PathVariable UUID id,
        @RequestBody @Validated UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    // ── Standard CRUD (no validation needed) ─────────────────────────────────

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Exception handlers ────────────────────────────────────────────────────

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NoSuchElementException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Handles ValidationException thrown by the service layer directly
     * (i.e. when validation runs outside the Spring MVC @Validated pipeline).
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleServiceValidation(ValidationException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ex.getMessage()
        );
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", ex.errors());
        return ResponseEntity.unprocessableEntity().body(problem);
    }
}
