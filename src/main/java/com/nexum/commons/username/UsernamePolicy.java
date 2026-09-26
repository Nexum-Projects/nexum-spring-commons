package com.nexum.commons.username;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Regla de username de la aplicación. Con la regla activa, {@link #normalize} recorta y pasa a minúsculas e
 * {@link #isValid} exige el patrón; desactivada ({@code nexum.commons.username.enabled=false}) solo recorta y acepta
 * cualquier valor. Se inyecta donde se guarda el username (el DTO valida con {@link Username}).
 */
public class UsernamePolicy {
    private final UsernameProperties properties;
    private final Pattern pattern;

    public UsernamePolicy(UsernameProperties properties) {
        this.properties = properties;
        this.pattern = Pattern.compile(properties.pattern());
    }

    public String normalize(String username) {
        if (username == null) {
            return null;
        }
        String trimmed = username.trim();
        return properties.enabled() ? trimmed.toLowerCase(Locale.ROOT) : trimmed;
    }

    public boolean isValid(String username) {
        return username != null && (!properties.enabled() || pattern.matcher(username).matches());
    }

    public String message() {
        return properties.message();
    }
}
