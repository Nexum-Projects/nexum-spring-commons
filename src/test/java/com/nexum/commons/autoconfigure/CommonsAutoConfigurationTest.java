package com.nexum.commons.autoconfigure;

import com.nexum.commons.error.GlobalExceptionHandler;
import com.nexum.commons.ratelimit.RateLimitFilter;
import com.nexum.commons.ratelimit.RateLimitProperties;
import com.nexum.commons.security.RestAccessDeniedHandler;
import com.nexum.commons.security.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommonsAutoConfigurationTest {
    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonsAutoConfiguration.class));

    @Test
    void registersHandlerAndFilterWithDefaultProperties() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(GlobalExceptionHandler.class).hasSingleBean(RateLimitFilter.class);
            RateLimitProperties properties = context.getBean(RateLimitProperties.class);
            assertThat(properties.pathPrefix()).isEqualTo("/api/v1/auth/");
            assertThat(properties.excludedPaths()).containsExactly("/api/v1/auth/change-password");
            assertThat(properties.authLimit()).isEqualTo(10);
        });
    }

    @Test
    void registersTheRestSecurityHandlers() {
        runner.run(context -> assertThat(context)
                .hasSingleBean(RestAuthenticationEntryPoint.class)
                .hasSingleBean(RestAccessDeniedHandler.class));
    }

    @Test
    void bindsPropertiesFromTheEnvironment() {
        runner.withPropertyValues(
                        "nexum.commons.rate-limit.path-prefix=/api/v2/auth/",
                        "nexum.commons.rate-limit.excluded-paths=/a,/b",
                        "nexum.commons.rate-limit.auth-limit=3")
                .run(context -> {
                    RateLimitProperties properties = context.getBean(RateLimitProperties.class);
                    assertThat(properties.pathPrefix()).isEqualTo("/api/v2/auth/");
                    assertThat(properties.excludedPaths()).isEqualTo(List.of("/a", "/b"));
                    assertThat(properties.authLimit()).isEqualTo(3);
                });
    }

    @Test
    void anApplicationHandlerReplacesTheLibraryOne() {
        runner.withBean("customHandler", GlobalExceptionHandler.class, CustomHandler::new)
                .run(context -> assertThat(context).hasSingleBean(GlobalExceptionHandler.class)
                        .getBean(GlobalExceptionHandler.class).isInstanceOf(CustomHandler.class));
    }

    @Test
    void rateLimitCanBeDisabled() {
        runner.withPropertyValues("nexum.commons.rate-limit.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(RateLimitFilter.class)
                        .hasSingleBean(GlobalExceptionHandler.class));
    }

    @Test
    void doesNothingOutsideAServletApplication() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CommonsAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(GlobalExceptionHandler.class)
                        .doesNotHaveBean(RateLimitFilter.class));
    }

    static class CustomHandler extends GlobalExceptionHandler {
    }
}
