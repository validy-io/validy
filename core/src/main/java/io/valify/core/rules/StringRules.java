package io.valify.core.rules;

import io.valify.core.Rule;
import io.valify.core.result.ValidationResult;

import java.util.regex.Pattern;

/**
 * Built-in rules for {@link String} values.
 *
 * <p>All rules treat {@code null} as a failing value unless noted otherwise.
 * Compose rules with {@code .and()}, {@code .or()}, and {@code .negate()}.
 */
public final class StringRules {

    private StringRules() {}

    // ── Presence ──────────────────────────────────────────────────────────────

    public static Rule<String> notNull() {
        return value -> value != null
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must not be null");
    }

    public static Rule<String> notBlank() {
        return value -> (value != null && !value.isBlank())
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must not be blank");
    }

    // ── Length ────────────────────────────────────────────────────────────────

    public static Rule<String> minLength(int min) {
        return value -> (value != null && value.length() >= min)
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must be at least %d characters".formatted(min));
    }

    public static Rule<String> maxLength(int max) {
        return value -> (value != null && value.length() <= max)
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must be at most %d characters".formatted(max));
    }

    public static Rule<String> length(int min, int max) {
        return minLength(min).and(maxLength(max));
    }

    // ── Pattern matching ──────────────────────────────────────────────────────

    public static Rule<String> matches(String regex) {
        return matches(Pattern.compile(regex));
    }

    public static Rule<String> matches(Pattern pattern) {
        return value -> (value != null && pattern.matcher(value).matches())
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must match pattern: %s".formatted(pattern));
    }

    // ── Semantic formats ──────────────────────────────────────────────────────

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.]{2,}$");

    private static final Pattern URL_PATTERN =
            Pattern.compile("^https?://[\\w\\-.]+(:\\d+)?(/[^\\s]*)?$");

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static Rule<String> email() {
        return matches(EMAIL_PATTERN)
                .withMessage("$", "must be a valid email address");
    }

    public static Rule<String> url() {
        return matches(URL_PATTERN)
                .withMessage("$", "must be a valid URL (http/https)");
    }

    public static Rule<String> uuid() {
        return matches(UUID_PATTERN)
                .withMessage("$", "must be a valid UUID");
    }

    public static Rule<String> numeric() {
        return value -> (value != null && value.chars().allMatch(Character::isDigit))
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must contain only digits");
    }

    public static Rule<String> alpha() {
        return value -> (value != null && value.chars().allMatch(Character::isLetter))
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must contain only letters");
    }

    // ── Content ───────────────────────────────────────────────────────────────

    public static Rule<String> startsWith(String prefix) {
        return value -> (value != null && value.startsWith(prefix))
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must start with \"%s\"".formatted(prefix));
    }

    public static Rule<String> endsWith(String suffix) {
        return value -> (value != null && value.endsWith(suffix))
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must end with \"%s\"".formatted(suffix));
    }

    public static Rule<String> contains(String substring) {
        return value -> (value != null && value.contains(substring))
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must contain \"%s\"".formatted(substring));
    }

    public static Rule<String> oneOf(String... allowed) {
        var set = java.util.Set.of(allowed);
        return value -> set.contains(value)
                ? ValidationResult.valid()
                : ValidationResult.invalid("$", "must be one of: %s".formatted(String.join(", ", allowed)));
    }
}