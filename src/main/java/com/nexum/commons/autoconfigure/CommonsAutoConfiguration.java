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

/**
 * Registra el manejador de errores, el rate limit y la regla de username en aplicaciones servlet. Cada bean se reemplaza declarando
 * uno propio del mismo tipo.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties({RateLimitProperties.class, UsernameProperties.class})
public class CommonsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "nexum.commons.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    public RateLimitFilter rateLimitFilter(RateLimitProperties properties) {
        return new RateLimitFilter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public UsernamePolicy usernamePolicy(UsernameProperties properties) {
        return new UsernamePolicy(properties);
    }

    /** Handlers 401/403 con el ErrorDTO estándar. La aplicación los registra en su SecurityFilterChain. */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(AuthenticationEntryPoint.class)
    static class SecurityHandlers {
        @Bean
        @ConditionalOnMissingBean
        RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
            return new RestAuthenticationEntryPoint();
        }

        @Bean
        @ConditionalOnMissingBean
        RestAccessDeniedHandler restAccessDeniedHandler() {
            return new RestAccessDeniedHandler();
        }
    }
}
