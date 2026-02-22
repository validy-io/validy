package io.validy.spring;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translates Spring MVC's validation failure exception into a clean
 * <a href="https://www.rfc-editor.org/rfc/rfc7807">RFC 7807 Problem Detail</a> JSON response.
 *
 * <h2>Response shape</h2>
 * <pre>{@code
 * HTTP/1.1 422 Unprocessable Entity
 * Content-Type: application/problem+json
 *
 * {
 *   "type":     "https://validy.io/problems/validation-error",
 *   "title":    "Validation Failed",
 *   "status":   422,
 *   "detail":   "3 constraint(s) violated",
 *   "errors": {
 *     "email":    ["must be a valid email address"],
 *     "name":     ["must not be blank"],
 *     "":         ["passwords do not match"]   ← object-level errors under ""
 *   }
 * }
 * }</pre>
 *
 * <p>Register this as a bean — or extend it — to customise the response shape.
 * It deliberately does NOT extend {@code ResponseEntityExceptionHandler} to
 * avoid pulling in Spring's full exception handling hierarchy.
 */
@RestControllerAdvice
public class ValidationExceptionHandler {

    private static final URI PROBLEM_TYPE =
            URI.create("https://validy.io/problems/validation-error");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {

        // Group field errors by field name; object-level errors land under ""
        Map<String, List<String>> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e instanceof FieldError fe ? fe.getField() : "",
                        Collectors.mapping(e -> e.getDefaultMessage(), Collectors.toList())
                ));

        int count = errors.values().stream().mapToInt(List::size).sum();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "%d constraint(s) violated".formatted(count)
        );
        problem.setType(PROBLEM_TYPE);
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", errors);

        return problem;
    }
}
