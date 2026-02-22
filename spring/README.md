# valify-spring-boot — Spring Boot Integration

Zero-magic Spring Boot integration for Valify. No annotation processors,
no AOP, no classpath scanning. Everything is explicit.

---

## How It Works (the full picture in 4 lines)

1. You declare a `ValidatorRegistry` `@Bean` listing your validators explicitly.
2. Auto-configuration detects the registry and creates a `ValifyValidator` bean.
3. `ValifyValidator` implements Spring's `Validator` SPI — Spring MVC already knows how to use it.
4. `@Valid` / `@Validated` on `@RequestBody` parameters triggers it automatically.

That's it. No other annotations, no proxies, no reflection on your domain objects.

---

## Setup

### Dependency (Maven)
```xml
<dependency>
    <groupId>io.valify</groupId>
    <artifactId>valify-spring-boot</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```kotlin
implementation("io.valify:valify-spring-boot:1.0.0")
```

---

## Usage

### Step 1 — Define your validators (pure Java, no Spring)

```java
class AppValidators {

    static Validator<CreateUserRequest> createUser() {
        return validator(CreateUserRequest.class)
            .field("name",  CreateUserRequest::name,  notBlank(), length(2, 100))
            .field("email", CreateUserRequest::email, notBlank(), email())
            .field("age",   CreateUserRequest::age,   between(18, 120))
            .build();
    }
}
```

### Step 2 — Register them in a @Configuration

```java
@Configuration
class ValifyConfig {

    @Bean
    ValidatorRegistry validatorRegistry() {
        return ValidatorRegistry.builder()
            .register(CreateUserRequest.class, AppValidators.createUser())
            .register(UpdateUserRequest.class, AppValidators.updateUser())
            .build();
    }

    @Bean
    ValidationExceptionHandler validationExceptionHandler() {
        return new ValidationExceptionHandler();
    }
}
```

### Step 3 — Use @Valid in your controllers

```java
@PostMapping("/users")
ResponseEntity<String> create(@RequestBody @Validated CreateUserRequest req) {
    // validation already passed if we're here
    return ResponseEntity.ok("created");
}
```

### Step 4 — Get clean error responses automatically

```json
HTTP 422 Unprocessable Entity
Content-Type: application/problem+json

{
  "type":   "https://valify.io/problems/validation-error",
  "title":  "Validation Failed",
  "status": 422,
  "detail": "2 constraint(s) violated",
  "errors": {
    "email": ["must be a valid email address"],
    "name":  ["must not be blank"]
  }
}
```

---

## Using Both Valify and Hibernate Validator

If you want Bean Validation annotations (`@NotNull`, `@Size`, etc.) *and* Valify
rules to both run, replace the `WebMvcConfigurer` with a delegating validator:

```java
@Bean
WebMvcConfigurer valifyMvcConfigurer(
    ValifyValidator valifyValidator,
    javax.validation.Validator beanValidator
) {
    return new WebMvcConfigurer() {
        @Override
        public Validator getValidator() {
            // Run Valify first, then Hibernate — errors from both are collected
            return new CompositeValidator(
                valifyValidator,
                new SpringValidatorAdapter(beanValidator)
            );
        }
    };
}
```

---

## Validating Outside Controllers (service layer, batch jobs, tests)

Valify validators are plain objects — just call `.validate()` directly:

```java
@Service
class UserService {

    private final Validator<CreateUserRequest> validator = AppValidators.createUser();

    public void create(CreateUserRequest req) {
        switch (validator.validate(req)) {
            case Valid   v  -> persist(req);
            case Invalid iv -> throw new ValidationException(iv.summary());
        }
    }
}
```

No Spring context needed. Validators are fully unit-testable in isolation.

---

## What the Auto-Configuration Does (and Does Not Do)

| Does | Does Not |
|------|----------|
| Create `ValifyValidator` if `ValidatorRegistry` bean exists | Touch anything if no registry is defined |
| Register Valify as Spring MVC's default validator | Override a `WebMvcConfigurer` you've already defined |
| Translate `MethodArgumentNotValidException` to RFC 7807 | Register `ValidationExceptionHandler` automatically (you opt in) |

All conditions use `@ConditionalOnBean` / `@ConditionalOnMissingBean` — your
explicit beans always win.