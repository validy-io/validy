package io.validy.spring.sample;

import io.validy.spring.sample.model.Address;
import io.validy.spring.sample.model.CreateUserRequest;
import io.validy.spring.sample.validators.UserValidators;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class UserApiTest {

    @Autowired MockMvc mockMvc;

    // ── Pure unit tests — no Spring context needed ────────────────────────────

    @Test
    void validRequest_passes() {
        var request = validCreateRequest();
        assertThat(UserValidators.createUser().validate(request).isValid()).isTrue();
    }

    @Test
    void blankName_fails_with_correct_field() {
        var request = new CreateUserRequest(
            "", "alice@example.com", 30, "P@ssw0rd!", "P@ssw0rd!",
            validAddress(), List.of("USER")
        );
        var result = UserValidators.createUser().validate(request);
        assertThat(result.isValid()).isFalse();
        result.ifInvalid(e ->
            assertThat(e.errors()).anyMatch(err -> err.field().equals("name"))
        );
    }

    @Test
    void passwordMismatch_fails_on_confirmPassword_field() {
        var request = new CreateUserRequest(
            "Alice", "alice@example.com", 30, "P@ssw0rd!", "different",
            validAddress(), List.of("USER")
        );
        var result = UserValidators.createUser().validate(request);
        result.ifInvalid(e ->
            assertThat(e.errors()).anyMatch(err -> err.field().equals("confirmPassword"))
        );
    }

    @Test
    void seniorRole_without_correct_age_fails() {
        var request = new CreateUserRequest(
            "Alice", "alice@example.com", 30, "P@ssw0rd!", "P@ssw0rd!",
            validAddress(), List.of("SENIOR")   // age 30 < 65
        );
        assertThat(UserValidators.createUser().validate(request).isValid()).isFalse();
    }

    @Test
    void nestedAddress_badZip_shows_prefixed_field() {
        var request = new CreateUserRequest(
            "Alice", "alice@example.com", 30, "P@ssw0rd!", "P@ssw0rd!",
            new Address("123 Main St", "Springfield", "bad-zip"),
            List.of("USER")
        );
        var result = UserValidators.createUser().validate(request);
        result.ifInvalid(e ->
            assertThat(e.errors()).anyMatch(err -> err.field().equals("address.zip"))
        );
    }

    @Test
    void weakPassword_reports_all_violations_at_once() {
        var request = new CreateUserRequest(
            "Alice", "alice@example.com", 30, "weak", "weak",
            validAddress(), List.of("USER")
        );
        var result = UserValidators.createUser().validate(request);
        result.ifInvalid(e ->
            // minLength + uppercase + digit + special character — all collected
            assertThat(e.errors().stream().filter(err -> err.field().equals("password")))
                .hasSizeGreaterThan(1)
        );
    }

    // ── HTTP integration tests ────────────────────────────────────────────────

    @Test
    void POST_users_with_invalid_body_returns_422() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "",
                      "email": "not-an-email",
                      "age": -1,
                      "password": "weak",
                      "confirmPassword": "weak",
                      "address": { "street": "", "city": "NYC", "zip": "bad" },
                      "roles": []
                    }
                """))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void POST_users_with_valid_body_returns_201() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Alice Dupont",
                      "email": "alice@example.com",
                      "age": 30,
                      "password": "P@ssw0rd!",
                      "confirmPassword": "P@ssw0rd!",
                      "address": { "street": "10 Main St", "city": "Austin", "zip": "78701" },
                      "roles": ["USER"]
                    }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Alice Dupont"))
            .andExpect(jsonPath("$.id").exists());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static CreateUserRequest validCreateRequest() {
        return new CreateUserRequest(
            "Alice Dupont", "alice@example.com", 30,
            "P@ssw0rd!", "P@ssw0rd!",
            validAddress(), List.of("USER")
        );
    }

    private static Address validAddress() {
        return new Address("10 Main St", "Austin", "78701");
    }
}
