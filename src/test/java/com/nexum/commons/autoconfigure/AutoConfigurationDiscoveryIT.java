package com.nexum.commons.autoconfigure;

import com.nexum.commons.error.BusinessException;
import com.nexum.commons.error.CommonErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Una aplicación real que solo tiene la librería en el classpath: el manejador de errores y el rate limit
 * aparecen por el archivo AutoConfiguration.imports, sin registrarlos a mano.
 */
@SpringBootTest(properties = {"nexum.commons.rate-limit.auth-limit=2", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
@Import(AutoConfigurationDiscoveryIT.TestConfig.class)
class AutoConfigurationDiscoveryIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void businessExceptionsBecomeTheStandardErrorBody() throws Exception {
        mvc.perform(get("/boom"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.type").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("nope"));
    }

    @Test
    void authEndpointsAreRateLimited() throws Exception {
        mvc.perform(post("/api/v1/auth/register")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/auth/register")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/auth/register"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {
        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer("postgres:16-alpine");
        }

        @Bean
        SecurityFilterChain permitAll(HttpSecurity http) throws Exception {
            return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
        }

        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {
        @GetMapping("/boom")
        void boom() {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "nope");
        }

        @PostMapping("/api/v1/auth/register")
        void register() {
        }
    }
}
