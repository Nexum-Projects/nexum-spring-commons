package com.nexum.commons.username;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Propiedades {@code nexum.commons.username.*}. Por defecto el username es un slug: 3 a 32 caracteres, empieza con
 * letra minúscula y sigue con minúsculas, dígitos o guion bajo.
 */
@ConfigurationProperties("nexum.commons.username")
public record UsernameProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue(DEFAULT_PATTERN) String pattern,
        @DefaultValue(DEFAULT_MESSAGE) String message
) {
    public static final String DEFAULT_PATTERN = "^[a-z][a-z0-9_]{2,31}$";
    public static final String DEFAULT_MESSAGE =
            "Username must be 3-32 characters: start with a letter, then lowercase letters, digits or underscore";

    public static UsernameProperties defaults() {
        return new UsernameProperties(true, DEFAULT_PATTERN, DEFAULT_MESSAGE);
    }
}
