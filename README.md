# Validy — Elegant Validation for Java 24

A composable, readable validation library built with modern Java idioms:
sealed types, records, pattern matching, and functional interfaces.

## Design Goals

- **Readable DSL** — validation code reads like a specification
- **Composable rules** — combine rules with `.and()`, `.or()`, `.negate()`
- **Reusable rules** — define named rules once, use everywhere
- **Zero dependencies** — pure Java 21+, no annotation processors
- **Exhaustive results** — `sealed` result type forces you to handle both cases

---

## Quick Start

```java


var validator = validator(User.class)
        .field("name", User::name, notBlank(), maxLength(100))
        .field("email", User::email, notBlank(), email())
        .field("age", User::age, between(0, 150))
        .build();

switch(validator.

validate(user)){
        case

Valid()        ->System.out.

println("✅ All good!");
    case

Invalid(var e) ->System.err.

println(e.summary());
        }
```

---

## Defining Custom Rules

Rules are plain functional interfaces — write a lambda or a method:

```java
// Inline custom rule
Rule<String> strongPassword = minLength(8)
    .and(matches(".*[A-Z].*").withMessage("$", "must contain uppercase"))
    .and(matches(".*[0-9].*").withMessage("$", "must contain a digit"));

// As a reusable static method
static Rule<String> ukPostcode() {
    return matches("^[A-Z]{1,2}\\d[\\dA-Z]? ?\\d[A-Z]{2}$")
        .withMessage("$", "must be a valid UK postcode");
}

// From a predicate + message
Rule<User> adultOnly = rule(u -> u.age() >= 18, "must be 18 or older");
```

---

## Composing Rules

```java
// Both must pass (collects all errors)
Rule<String> nameRule = notBlank().and(maxLength(100));

// First success wins
Rule<String> flexibleId = uuid().or(numeric());

// Invert a rule
Rule<String> notAnEmail = email().negate("must NOT be an email address");
```

---

## Cross-field & Conditional Validation

```java
var validator = validator(PasswordForm.class)
    // Cross-field rule
    .rule(form -> form.password().equals(form.confirm())
        ? ValidationResult.valid()
        : ValidationResult.invalid("confirm", "passwords do not match"))

    // Conditional rule
    .when(
        form -> form.roles().contains("ADMIN"),
        rule(form -> form.age() >= 21, "admins must be 21+")
    )
    .build();
```

---

## Nested Objects

```java
var addressValidator = validator(Address.class)
    .field("street", Address::street, notBlank())
    .field("zip",    Address::zip,    usZip())
    .build();

var userValidator = validator(User.class)
    .field("name", User::name, notBlank())
    .nested("address", User::address, addressValidator)  // errors prefixed as "address.street"
    .build();
```

---

## Handling Results

```java
var result = validator.validate(value);

// Pattern matching (exhaustive — compiler enforces all cases)
switch (result) {
    case Valid()        -> proceed();
    case Invalid(var e) -> showErrors(e.errors());
}

// Callbacks
result.ifValid(() -> System.out.println("ok"));
result.ifInvalid(e -> log.warn(e.summary()));

// Boolean
if (result.isValid()) { ... }
```

---

## Built-in Rules

### String
| Rule | Description |
|------|-------------|
| `notBlank()` | Not null and not whitespace-only |
| `notNull()` | Not null |
| `minLength(n)` / `maxLength(n)` | Length bounds |
| `length(min, max)` | Combined length check |
| `email()` | Valid email format |
| `url()` | Valid http/https URL |
| `uuid()` | Valid UUID |
| `matches(regex)` | Custom regex pattern |
| `numeric()` | Digits only |
| `alpha()` | Letters only |
| `oneOf(...)` | Whitelist check |
| `startsWith(s)` / `endsWith(s)` | Prefix/suffix check |

### Number (`Comparable`)
| Rule | Description |
|------|-------------|
| `min(n)` | Value ≥ n |
| `max(n)` | Value ≤ n |
| `between(min, max)` | Value in [min, max] |
| `positive()` | Integer > 0 |
| `nonNegative()` | Integer ≥ 0 |

### Collection
| Rule | Description |
|------|-------------|
| `notEmpty()` | Collection is not empty |
| `minSize(n)` | Collection has ≥ n elements |
| `maxSize(n)` | Collection has ≤ n elements |

---

## Project Structure

```
io.validy/
├── Validy.java              ← Static façade (single import)
├── core/
│   ├── Rule.java            ← Functional interface + combinators
│   └── Validator.java       ← Fluent builder
├── result/
│   ├── ValidationResult.java ← Sealed Valid | Invalid
│   └── ValidationError.java  ← Field + message record
└── rules/
    ├── StringRules.java
    ├── NumberRules.java
    └── CollectionRules.java
```

---

## Extending Validy

To add a new rule category, just write a class with static factory methods
returning `Rule<YourType>`:

```java
public final class DateRules {
    public static Rule<LocalDate> past() {
        return date -> date != null && date.isBefore(LocalDate.now())
            ? ValidationResult.valid()
            : ValidationResult.invalid("$", "must be in the past");
    }

    public static Rule<LocalDate> after(LocalDate reference) {
        return date -> date != null && date.isAfter(reference)
            ? ValidationResult.valid()
            : ValidationResult.invalid("$", "must be after %s".formatted(reference));
    }
}
```

That's it. No registration, no reflection, no annotations needed.