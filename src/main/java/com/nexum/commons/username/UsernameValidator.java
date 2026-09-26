package com.nexum.commons.username;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validador de {@link Username}. Con el Validator de Spring recibe el {@link UsernamePolicy} de la aplicación; fuera de
 * Spring usa la regla por defecto.
 */
public class UsernameValidator implements ConstraintValidator<Username, String> {
    private final UsernamePolicy policy;

    public UsernameValidator() {
        this(new UsernamePolicy(UsernameProperties.defaults()));
    }

    @Autowired
    public UsernameValidator(UsernamePolicy policy) {
        this.policy = policy;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || policy.isValid(value)) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(escape(policy.message())).addConstraintViolation();
        return false;
    }

    /** El mensaje viene de propiedades: se escapa para que no se interprete como plantilla ({@code {}}, {@code $}). */
    private static String escape(String message) {
        return message.replace("\\", "\\\\").replace("{", "\\{").replace("}", "\\}").replace("$", "\\$");
    }
}
