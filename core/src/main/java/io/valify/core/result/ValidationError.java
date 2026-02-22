package io.valify.core.result;

/**
 * A single validation failure — field path + human-readable message.
 */
public record ValidationError(String field, String message) {

    /** Convenience constructor for top-level (field-less) errors. */
    public static ValidationError of(String message) {
        return new ValidationError("$", message);
    }

    public static ValidationError of(String field, String message) {
        return new ValidationError(field, message);
    }

    @Override
    public String toString() {
        return "[%s] %s".formatted(field, message);
    }
}