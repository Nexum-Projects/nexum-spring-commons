package com.nexum.commons.autoconfigure;

import com.nexum.commons.error.GlobalExceptionHandler;
import com.nexum.commons.ratelimit.RateLimitFilter;
import com.nexum.commons.ratelimit.RateLimitProperties;
import com.nexum.commons.security.RestAccessDeniedHandler;
import com.nexum.commons.security.RestAuthenticationEntryPoint;
import com.nexum.commons.username.UsernamePolicy;
import com.nexum.commons.username.UsernameProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Registra el manejador de errores, el rate limit, la regla de username y los handlers 401/403 en aplicaciones servlet.
 * Los beans llevan el prefijo {@code nexumCommons} para no chocar con beans de la aplicación que se llamen igual.
 * Cada uno se reemplaza declarando uno propio del mismo tipo; el entry point y el handler de 403 no se registran si la
 * aplicación ya tiene su propio {@code AuthenticationEntryPoint} o {@code AccessDeniedHandler}.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties({RateLimitProperties.class, UsernameProperties.class})
public class CommonsAutoConfiguration {

    @Bean("nexumCommonsGlobalExceptionHandler")
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean("nexumCommonsRateLimitFilter")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "nexum.commons.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    public RateLimitFilter rateLimitFilter(RateLimitProperties properties) {
        return new RateLimitFilter(properties);
    }

    @Bean("nexumCommonsUsernamePolicy")
    @ConditionalOnMissingBean
    public UsernamePolicy usernamePolicy(UsernameProperties properties) {
        return new UsernamePolicy(properties);
    }

    /** Handlers 401/403 con el ErrorDTO estándar. La aplicación los registra en su SecurityFilterChain. */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(AuthenticationEntryPoint.class)
    static class SecurityHandlers {
        @Bean("nexumCommonsAuthenticationEntryPoint")
        @ConditionalOnMissingBean(AuthenticationEntryPoint.class)
        RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
            return new RestAuthenticationEntryPoint();
        }

        @Bean("nexumCommonsAccessDeniedHandler")
        @ConditionalOnMissingBean(AccessDeniedHandler.class)
        RestAccessDeniedHandler restAccessDeniedHandler() {
            return new RestAccessDeniedHandler();
        }
    }
}
