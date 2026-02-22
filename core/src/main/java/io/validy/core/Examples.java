package io.validy.core;

import io.validy.core.result.ValidationResult;

import java.util.List;

import static io.validy.core.Validy.*;

/**
 * End-to-end examples showing the Validy DSL.
 *
 * Run main() to see validation output.
 */
public class Examples {

    // ── Domain records (Java 16+) ─────────────────────────────────────────────

    record Address(String street, String city, String zip) {}

    record User(
            String name,
            String email,
            int age,
            String password,
            String confirmPassword,
            Address address,
            List<String> roles
    ) {}

    // ── 1. Custom reusable rules ──────────────────────────────────────────────

    /** A strong password rule — composing smaller rules into one named concept. */
    static Rule<String> strongPassword() {
        return minLength(8)
                .and(matches(".*[A-Z].*").withMessage("$", "must contain an uppercase letter"))
                .and(matches(".*[0-9].*").withMessage("$", "must contain a digit"))
                .and(matches(".*[!@#$%^&*].*").withMessage("$", "must contain a special character"));
    }

    /** A reusable UK postcode rule — simple regex, nicely named. */
    static Rule<String> ukPostcode() {
        return matches("^[A-Z]{1,2}\\d[\\dA-Z]? ?\\d[A-Z]{2}$")
                .withMessage("$", "must be a valid UK postcode");
    }

    /** A US ZIP code rule. */
    static Rule<String> usZip() {
        return matches("^\\d{5}(-\\d{4})?$")
                .withMessage("$", "must be a valid US ZIP code");
    }

    // ── 2. Nested address validator ───────────────────────────────────────────

    static Validator<Address> addressValidator() {
        return validator(Address.class)
                .field("street", Address::street, notBlank(), maxLength(200))
                .field("city",   Address::city,   notBlank(), maxLength(100))
                .field("zip",    Address::zip,    notBlank(), usZip())
                .build();
    }

    // ── 3. Full user validator ────────────────────────────────────────────────

    static Validator<User> userValidator() {
        return validator(User.class)
                .field("name",     User::name,    notBlank(), length(2, 100))
                .field("email",    User::email,   notBlank(), email())
                .field("age",      User::age,     between(0, 150))
                .field("password", User::password, strongPassword())
                .field("roles",    User::roles,    notEmpty(), maxSize(10))

                // Cross-field rule: passwords must match
                .rule(u -> u.password().equals(u.confirmPassword())
                        ? ValidationResult.valid()
                        : ValidationResult.invalid("confirmPassword", "passwords do not match"))

                // Conditional rule: seniors must be 65+
                .when(
                        u -> u.roles().contains("SENIOR"),
                        rule(u -> u.age() >= 65, "SENIOR role requires age ≥ 65")
                )

                // Nested validator: address uses its own validator
                .nested("address", User::address, addressValidator())

                .build();
    }

    // ── 4. Main demo ──────────────────────────────────────────────────────────

    public static void main(String[] args) {
        var validator = userValidator();

        // ✅ Valid user
        var validUser = new User(
                "Alice Dupont",
                "alice@example.com",
                30,
                "S3cur3!Pass",
                "S3cur3!Pass",
                new Address("10 Downing Street", "London", "12345"),
                List.of("USER", "ADMIN")
        );

        System.out.println("=== Valid user ===");
        switch (validator.validate(validUser)) {
            case ValidationResult.Valid    v -> System.out.println("✅ Validation passed!");
            case ValidationResult.Invalid  e -> System.out.println("❌ " + e.summary());
        }

        // ❌ Invalid user — multiple field errors
        var badUser = new User(
                "",                   // blank name
                "not-an-email",       // bad email
                -5,                   // negative age
                "weak",               // weak password
                "different",          // mismatched confirm
                new Address("", "London", "bad-zip"),  // blank street, bad zip
                List.of()             // empty roles
        );

        System.out.println("\n=== Invalid user ===");
        switch (validator.validate(badUser)) {
            case ValidationResult.Valid    v -> System.out.println("✅ Validation passed!");
            case ValidationResult.Invalid  e -> System.out.println(e.summary());
        }

        // ── Inline one-off validation ──────────────────────────────────────────
        System.out.println("\n=== Inline rule ===");
        Rule<String> strictEmail = notBlank().and(email()).and(maxLength(254));
        strictEmail.validate("bad@@address")
                .ifInvalid(e -> System.out.println("Email errors: " + e.errors()));
    }
}