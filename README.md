<div align="center">

<img src="https://img.shields.io/badge/Java-24-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 24"/>
<img src="https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4"/>
<img src="https://img.shields.io/badge/Jakarta_EE-11-blue?style=for-the-badge" alt="Jakarta EE 11"/>
<img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="MIT License"/>
<img src="https://img.shields.io/badge/Zero-Dependencies-brightgreen?style=for-the-badge" alt="Zero Dependencies"/>

<br/>
<br/>

```
 ██╗   ██╗ █████╗ ██╗     ██╗██████╗ ██╗   ██╗
 ██║   ██║██╔══██╗██║     ██║██╔══██╗╚██╗ ██╔╝
 ██║   ██║███████║██║     ██║██║  ██║ ╚████╔╝
 ╚██╗ ██╔╝██╔══██║██║     ██║██║  ██║  ╚██╔╝
  ╚████╔╝ ██║  ██║███████╗██║██████╔╝   ██║
   ╚═══╝  ╚═╝  ╚═╝╚══════╝╚═╝╚═════╝   ╚═╝
```

### Elegant, composable validation for Java 24

*Fluent DSL · Sealed results · Validation groups · Spring Boot 4 ready · Zero dependencies*

<br/>

[Getting Started](#-getting-started) · [DSL Reference](#-dsl-reference) · [Spring Boot](#-spring-boot-integration) · [Modules](#-modules) · [Examples](#-examples)

</div>

---

## Why Validy?

Java validation has long meant one of two things: annotation soup on your domain objects, or writing the same `if (x == null)` checks in every service method. Validy takes a different approach.

```java
// Your domain object stays clean — no annotations
record User(String name, String email, int age, Address address) {}

// Rules live where they belong — in a dedicated validator
var validator = validator(User.class)
    .field("name",    User::name,    notBlank(), maxLength(100))
    .field("email",   User::email,   notBlank(), email())
    .field("age",     User::age,     between(18, 120))
    .nested("address", User::address, addressValidator())
    .build();

// Results are sealed — the compiler forces you to handle both cases
switch (validator.validate(user)) {
    case Valid   v -> proceed(user);
    case Invalid e -> respond(e.summary());
}
```

No reflection. No annotation processors. No classpath scanning. Plain Java 24.

---

## ✨ Highlights

| | |
|---|---|
| 🧩 **Composable rules** | Combine with `.and()`, `.or()`, `.negate()` |
| 🏷️ **Validation groups** | Scope rules to `OnCreate`, `OnUpdate`, or any custom phase |
| 🔗 **Nested validation** | Embed validators for child objects — errors prefixed automatically |
| 📋 **Collection elements** | Validate every item in a `List` or `Set` with `eachElement()` |
| 🔒 **Sealed results** | `Valid` / `Invalid` — pattern matching, no unchecked casts |
| 🌱 **Spring Boot 4** | Drop-in integration — `@Validated` just works, RFC 7807 responses |
| 🪶 **Zero dependencies** | `core` module is pure Java 24, nothing else |
| 🧪 **Testable by design** | Validators are plain objects — `new` them in unit tests, no context needed |

---

## 📦 Modules

```
validy/
├── core/                     Pure Java 24 — DSL, rules, sealed result type
├── spring/                   Spring Boot 4 auto-configuration
└── samples/
    └── spring-boot-sample/   Full REST API demonstrating all features
```

### `core`
The heart of the library. Zero dependencies — just Java 24 sealed types, records, and functional interfaces. Use this alone if you're not in a Spring environment.

### `spring`
Spring Boot 4 integration. Implements Spring's `Validator` SPI so `@Validated` on `@RequestBody` works automatically. Provides RFC 7807 Problem Detail error responses out of the box.

### `samples/spring-boot-sample`
A fully working Spring Boot 4 REST API showing every feature: `@Validated` controllers, service-layer manual validation, custom reusable rules, nested validators, and collection element validation.

---

## 🚀 Getting Started

### Gradle (Kotlin DSL)

```kotlin
// Core only — no Spring required
implementation("io.validy:core:1.0.0")

// Spring Boot 4 integration
implementation("io.validy:spring:1.0.0")
```

### Maven

```xml
<!-- Core only -->
<dependency>
    <groupId>io.validy</groupId>
    <artifactId>core</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Spring Boot 4 integration -->
<dependency>
    <groupId>io.validy</groupId>
    <artifactId>spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 📖 DSL Reference

### Defining a validator

```java
import static validy.core.Valify.*;

var userValidator = validator(User.class)
    .field("name",  User::name,  notBlank(), maxLength(100))
    .field("email", User::email, notBlank(), email())
    .field("age",   User::age,   between(18, 120))
    .build();
```

### Running validation

```java
// Basic — runs all Default rules
ValidationResult result = userValidator.validate(user);

// With groups — runs Default + specified groups
ValidationResult result = userValidator.validate(user, OnCreate.class);

// Pattern matching on the result
switch (result) {
    case Valid   v -> System.out.println("All good!");
    case Invalid e -> System.err.println(e.summary());
}

// Callbacks
result.ifValid(() -> persist(user));
result.ifInvalid(e -> log.warn(e.summary()));

// Boolean check
if (result.isValid()) { ... }
```

### Custom rules

Rules are plain functional interfaces — a lambda, method reference, or class all work:

```java
// Inline
Rule<String> strongPassword = minLength(8)
    .and(matches(".*[A-Z].*").withMessage("$", "must contain an uppercase letter"))
    .and(matches(".*[0-9].*").withMessage("$", "must contain a digit"))
    .and(matches(".*[!@#$%^&*].*").withMessage("$", "must contain a special character"));

// Reusable static method
static Rule<String> ukPostcode() {
    return matches("^[A-Z]{1,2}\\d[\\dA-Z]? ?\\d[A-Z]{2}$")
        .withMessage("$", "must be a valid UK postcode");
}

// From a predicate + message
Rule<User> adultOnly = rule(u -> u.age() >= 18, "must be 18 or older");
```

### Composing rules

```java
// Both must pass — all errors collected
Rule<String> name = notBlank().and(maxLength(100));

// First success wins — short-circuits
Rule<String> id = uuid().or(numeric());

// Invert
Rule<String> notAnEmail = email().negate("must NOT be an email");

// Adapt to a different type
Rule<String> noSwearWords = ...;
Rule<Comment> cleanComment = noSwearWords.contramap(Comment::body);
```

### Nested objects

```java
var addressValidator = validator(Address.class)
    .field("street", Address::street, notBlank())
    .field("city",   Address::city,   notBlank())
    .field("zip",    Address::zip,    matches("^\\d{5}$"))
    .build();

var userValidator = validator(User.class)
    .field("name", User::name, notBlank())
    .nested("address", User::address, addressValidator)
    // child error "zip" surfaces as "address.zip"
    .build();
```

### Collection element validation

```java
// Validate each string element
.field("tags", Post::tags, notEmpty(), eachElement(notBlank()))

// Validate each object with its own validator
.field("addresses", User::addresses, notEmpty(), eachElement(addressValidator))

// Errors are indexed automatically:
// "[0].zip"    → first address, zip field
// "[1]"        → second element (object-level error)
// "[2].street" → third address, street field
```

### Validation groups

```java
// 1. Declare groups — plain interfaces, no annotations, no registration
public interface OnCreate extends ValidationGroup {}
public interface OnUpdate extends ValidationGroup {}

// 2. Assign rules to groups
var validator = validator(User.class)
    .field("name",     User::name,     notBlank())                          // Default — always
    .field("email",    User::email,    notBlank(), email())                 // Default — always
    .field("password", User::password, strongPassword()).groups(OnCreate.class) // create only
    .field("id",       User::id,       notNull()).groups(OnUpdate.class)        // update only
    .rule(u -> u.password().equals(u.confirm())
        ? valid() : invalid("confirm", "must match")).groups(OnCreate.class)
    .build();

// 3. Run with the right group
validator.validate(user);                    // Default only
validator.validate(user, OnCreate.class);   // Default + OnCreate
validator.validate(user, OnUpdate.class);   // Default + OnUpdate
validator.validate(user, OnCreate.class, OnPublish.class); // multiple groups
```

---

## 🛠 Built-in Rules

### String

| Rule | Description |
|------|-------------|
| `notBlank()` | Not null and not whitespace-only |
| `notNull()` | Not null |
| `minLength(n)` / `maxLength(n)` | Length bounds |
| `length(min, max)` | Combined length bounds |
| `email()` | Valid email address |
| `url()` | Valid http/https URL |
| `uuid()` | Valid UUID |
| `matches(regex)` | Custom regex pattern |
| `numeric()` | Digits only |
| `alpha()` | Letters only |
| `oneOf(values...)` | Whitelist |
| `startsWith(s)` / `endsWith(s)` | Prefix / suffix |

### Number

| Rule | Description |
|------|-------------|
| `min(n)` | Value ≥ n — works with any `Comparable` |
| `max(n)` | Value ≤ n |
| `between(min, max)` | Value in [min, max] |
| `positive()` | Integer > 0 |
| `nonNegative()` | Integer ≥ 0 |

### Collection

| Rule | Description |
|------|-------------|
| `notEmpty()` | Collection is not empty |
| `minSize(n)` | At least n elements |
| `maxSize(n)` | At most n elements |
| `eachElement(rule)` | Every element passes the rule — errors indexed as `[0]`, `[1].field` |

### Temporal (`Instant`)

| Rule | Description |
|------|-------------|
| `future()` | Strictly after now |
| `futureOrPresent()` | Now or after |
| `past()` | Strictly before now |
| `after(ref)` | After a fixed instant |
| `before(ref)` | Before a fixed instant |

---

## 🌱 Spring Boot Integration

### 1 — Register your validators

```java
@Configuration
class ValifyConfig {

    @Bean
    ValidatorRegistry validatorRegistry() {
        return ValidatorRegistry.builder()
            .register(CreateUserRequest.class, UserValidators.createUser())
            .register(UpdateUserRequest.class, UserValidators.updateUser())
            .build();
    }

    @Bean
    ValidationExceptionHandler validationExceptionHandler() {
        return new ValidationExceptionHandler();
    }
}
```

### 2 — Use `@Validated` in controllers

```java
@RestController
@RequestMapping("/users")
class UserController {

    @PostMapping
    ResponseEntity<UserResponse> create(@RequestBody @Validated CreateUserRequest req) {
        // Validation passed — proceed
        return ResponseEntity.ok(userService.create(req));
    }
}
```

### 3 — Automatic RFC 7807 error responses

```json
HTTP/1.1 422 Unprocessable Entity
Content-Type: application/problem+json

{
  "type":   "https://validy.io/problems/validation-error",
  "title":  "Validation Failed",
  "status": 422,
  "detail": "3 constraint(s) violated",
  "errors": {
    "email":          ["must be a valid email address"],
    "name":           ["must not be blank"],
    "address.zip":    ["must be a valid US ZIP code"]
  }
}
```

No additional configuration — `ValidationExceptionHandler` handles this automatically.

### Auto-configuration

The Spring module registers itself via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. It activates **only** when a `ValidatorRegistry` bean is present — zero impact if you don't define one.

---

## 💡 Examples

### Reusable rule library

```java
public final class AppRules {

    public static Rule<String> strongPassword() {
        return minLength(8)
            .and(matches(".*[A-Z].*").withMessage("$", "must contain an uppercase letter"))
            .and(matches(".*[0-9].*").withMessage("$", "must contain a digit"))
            .and(matches(".*[!@#$%^&*].*").withMessage("$", "must contain a special character"));
    }

    public static Rule<String> usZip() {
        return matches("^\\d{5}(-\\d{4})?$")
            .withMessage("$", "must be a valid US ZIP code");
    }

    public static Rule<String> slug() {
        return matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")
            .withMessage("$", "must be a valid URL slug");
    }
}
```

### Multi-step form validation

```java
interface Step1 extends ValidationGroup {}
interface Step2 extends ValidationGroup {}
interface Step3 extends ValidationGroup {}

var signupValidator = validator(SignupForm.class)
    .field("name",  SignupForm::name,  notBlank()).groups(Step1.class)
    .field("email", SignupForm::email, email())   .groups(Step1.class)
    .field("street", SignupForm::street, notBlank()).groups(Step2.class)
    .field("zip",    SignupForm::zip,    usZip())  .groups(Step2.class)
    .field("card",   SignupForm::card,   notBlank()).groups(Step3.class)
    .build();

// Each step validates only what the user has filled in so far
signupValidator.validate(form, Step1.class);
signupValidator.validate(form, Step1.class, Step2.class);
signupValidator.validate(form, Step1.class, Step2.class, Step3.class);
```

### Service-layer validation (no HTTP)

```java
@Service
class OrderService {

    private final Validator<CreateOrderRequest> validator = OrderValidators.create();

    public Order createOrder(CreateOrderRequest request) {
        switch (validator.validate(request, OnCreate.class)) {
            case Valid   v -> { return persist(request); }
            case Invalid e -> throw new ValidationException(e.errors());
        }
    }
}
```

### Unit testing validators

```java
class UserValidatorTest {

    // No Spring context — validators are plain objects
    private final Validator<CreateUserRequest> validator = UserValidators.createUser();

    @Test
    void blank_name_fails_on_name_field() {
        var req    = new CreateUserRequest("", "a@b.com", 25, "P@ssw0rd!", "P@ssw0rd!");
        var result = validator.validate(req, OnCreate.class);

        assertThat(result.isValid()).isFalse();
        result.ifInvalid(e ->
            assertThat(e.errors()).anyMatch(err -> err.field().equals("name"))
        );
    }

    @Test
    void valid_request_passes() {
        var req = new CreateUserRequest("Alice", "alice@example.com", 30, "P@ssw0rd!", "P@ssw0rd!");
        assertThat(validator.validate(req, OnCreate.class).isValid()).isTrue();
    }
}
```

---

## 🗺 Roadmap

- [ ] `GroupSequence` — ordered phase execution (gate expensive rules behind cheap ones)
- [ ] `LocalDate` / `LocalDateTime` rule set
- [ ] `BigDecimal` precision and scale rules
- [ ] Fail-fast mode (stop at first error)
- [ ] I18n / message bundle support

---

## 🤝 Contributing

Contributions are welcome. Please open an issue before submitting a pull request for significant changes.

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Commit your changes: `git commit -m 'feat: add my feature'`
4. Push to the branch: `git push origin feat/my-feature`
5. Open a pull request

---

## 📄 License

Validy is released under the [MIT License](LICENSE).

---

<div align="center">

Built with ☕ and a deep dislike of annotation-driven validation

**[validy-io/validy](https://github.com/validy-io/validy)**

</div>