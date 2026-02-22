package io.valify.spring.sample.validators;

import io.valify.core.Rule;
import io.valify.core.Validator;
import io.valify.core.result.ValidationResult;
import io.valify.spring.sample.model.*;

import static io.valify.core.rules.StringRules.matches;
import static io.valify.core.rules.StringRules.minLength;
import static io.valify.core.Valify.*;

/**
 * All application validators defined in one place.
 *
 * Pure Java — no Spring annotations, fully testable without a context.
 * Import the validator you need wherever you need it.
 */
public final class UserValidators {

    private UserValidators() {}

    // ── Custom reusable rules ─────────────────────────────────────────────────

    /**
     * A strong password rule composed from smaller primitives.
     * Reuse this anywhere a password field appears.
     */
    public static Rule<String> strongPassword() {
        return minLength(8)
            .and(matches(".*[A-Z].*").withMessage("$", "must contain an uppercase letter"))
            .and(matches(".*[0-9].*").withMessage("$", "must contain a digit"))
            .and(matches(".*[!@#$%^&*].*").withMessage("$", "must contain a special character"));
    }

    /**
     * US ZIP code — a named rule that can be shared across address validators.
     */
    public static Rule<String> usZip() {
        return matches("^\\d{5}(-\\d{4})?$")
            .withMessage("$", "must be a valid US ZIP code (e.g. 90210 or 90210-1234)");
    }

    /**
     * Role name — must be one of the known application roles.
     */
    public static Rule<String> knownRole() {
        return oneOf("USER", "ADMIN", "MODERATOR", "SENIOR")
            .withMessage("$", "must be one of: USER, ADMIN, MODERATOR, SENIOR");
    }

    // ── Nested address validator ──────────────────────────────────────────────

    public static Validator<Address> address() {
        return validator(Address.class)
            .field("street", Address::street, notBlank(), maxLength(200))
            .field("city",   Address::city,   notBlank(), maxLength(100))
            .field("zip",    Address::zip,    notBlank(), usZip())
            .build();
    }

    // ── CreateUserRequest validator ───────────────────────────────────────────

    public static Validator<CreateUserRequest> createUser() {
        return validator(CreateUserRequest.class)
            .field("name",     CreateUserRequest::name,     notBlank(), length(2, 100))
            .field("email",    CreateUserRequest::email,    notBlank(), email())
            .field("age",      CreateUserRequest::age,      between(18, 120))
            .field("password", CreateUserRequest::password, strongPassword())
            .field("roles",    CreateUserRequest::roles,    notEmpty(), maxSize(5))

            // Cross-field: passwords must match
            .rule(r -> r.password().equals(r.confirmPassword())
                ? ValidationResult.valid()
                : ValidationResult.invalid("confirmPassword", "passwords do not match"))

            // Conditional: SENIOR role requires age >= 65
            .when(
                r -> r.roles().contains("SENIOR"),
                rule(r -> r.age() >= 65, "SENIOR role requires age ≥ 65")
            )

            // Nested: address has its own validator, errors prefixed with "address."
            .nested("address", CreateUserRequest::address, address())

            .build();
    }

    // ── UpdateUserRequest validator ───────────────────────────────────────────

    public static Validator<UpdateUserRequest> updateUser() {
        return validator(UpdateUserRequest.class)
            .field("name",  UpdateUserRequest::name,  notBlank(), length(2, 100))
            .field("email", UpdateUserRequest::email, notBlank(), email())
            .build();
    }
}
