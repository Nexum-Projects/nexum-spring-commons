package com.nexum.commons.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Propiedades {@code nexum.commons.rate-limit.*}. Se limitan los POST bajo {@code pathPrefix} por IP y ruta, y
 * {@code loginPath} además por el email del cuerpo.
 */
@ConfigurationProperties("nexum.commons.rate-limit")
public record RateLimitProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("/api/v1/auth/") String pathPrefix,
        @DefaultValue("/api/v1/auth/change-password") List<String> excludedPaths,
        @DefaultValue("/api/v1/auth/login") String loginPath,
        @DefaultValue("10") int authLimit,
        @DefaultValue("5") int loginEmailLimit,
        @DefaultValue("60") long windowSeconds
) {
}
