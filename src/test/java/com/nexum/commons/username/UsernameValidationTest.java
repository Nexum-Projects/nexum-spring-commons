package com.nexum.commons.username;

import com.nexum.commons.autoconfigure.CommonsAutoConfiguration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** @Username con el Validator real de Spring: la regla se decide por propiedades, sin tocar el DTO. */
class UsernameValidationTest {
    record RegisterRequest(@Username String name) {
    }

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonsAutoConfiguration.class, ValidationAutoConfiguration.class));

    @Test
    void enabledByDefaultWithTheSlugRule() {
        runner.run(context -> {
            Validator validator = context.getBean(Validator.class);
            assertThat(validator.validate(new RegisterRequest("maria_lopez"))).isEmpty();

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(new RegisterRequest("María López"));
            assertThat(violations).singleElement().satisfies(v -> {
                assertThat(v.getPropertyPath()).hasToString("name");
                assertThat(v.getMessage()).isEqualTo(UsernameProperties.DEFAULT_MESSAGE);
            });
        });
    }

    @Test
    void canBeDisabledFromProperties() {
        runner.withPropertyValues("nexum.commons.username.enabled=false").run(context ->
                assertThat(context.getBean(Validator.class).validate(new RegisterRequest("María López"))).isEmpty());
    }

    @Test
    void patternAndMessageComeFromProperties() {
        runner.withPropertyValues(
                        "nexum.commons.username.pattern=^[a-z][a-z0-9-]{2,31}$",
                        "nexum.commons.username.message=Solo minúsculas, dígitos y guiones {no es una plantilla}")
                .run(context -> {
                    Validator validator = context.getBean(Validator.class);
                    assertThat(validator.validate(new RegisterRequest("maria-hernandez"))).isEmpty();
                    assertThat(validator.validate(new RegisterRequest("maria_hernandez")))
                            .singleElement()
                            .extracting(ConstraintViolation::getMessage)
                            .isEqualTo("Solo minúsculas, dígitos y guiones {no es una plantilla}");
                });
    }

    @Test
    void nullIsLeftToNotBlank() {
        runner.run(context ->
                assertThat(context.getBean(Validator.class).validate(new RegisterRequest(null))).isEmpty());
    }

    @Test
    void policyBeanIsAvailableAndReplaceable() {
        runner.run(context -> assertThat(context).hasSingleBean(UsernamePolicy.class));
        runner.withBean("customPolicy", UsernamePolicy.class,
                        () -> new UsernamePolicy(new UsernameProperties(false, "x", "y")))
                .run(context -> assertThat(context.getBean(UsernamePolicy.class).isValid("ANY thing")).isTrue());
    }
}
