package io.validy.core;


/**
 * Marker interface for validation groups.
 *
 * <p>A group is just a type — create your own by declaring an interface or a
 * record that implements {@code ValidationGroup}. No annotations, no enums,
 * no registration required.
 *
 * <h2>Declaring groups</h2>
 * <pre>{@code
 * // Option A — nested inside your validator class (recommended)
 * public final class UserValidators {
 *     public interface OnCreate  extends ValidationGroup {}
 *     public interface OnUpdate  extends ValidationGroup {}
 *     public interface OnPublish extends ValidationGroup {}
 * }
 *
 * // Option B — top-level shared groups
 * public interface Groups {
 *     interface OnCreate  extends ValidationGroup {}
 *     interface OnUpdate  extends ValidationGroup {}
 * }
 * }</pre>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * var validator = validator(User.class)
 *     .field("name",     User::name,     notBlank())                         // always runs
 *     .field("email",    User::email,    notBlank(), email())                // always runs
 *     .field("password", User::password, strongPassword()).groups(OnCreate)  // OnCreate only
 *     .field("id",       User::id,       notNull())      .groups(OnUpdate)  // OnUpdate only
 *     .build();
 *
 * validator.validate(user);                    // runs all rules
 * validator.validate(user, OnCreate.class);   // runs "always" + OnCreate rules
 * validator.validate(user, OnUpdate.class);   // runs "always" + OnUpdate rules
 * }</pre>
 */
public interface ValidationGroup {

    /**
     * Built-in group that always runs regardless of which group is requested.
     * Rules with no explicit group belong to this group implicitly.
     */
    interface Default extends ValidationGroup {}
}
