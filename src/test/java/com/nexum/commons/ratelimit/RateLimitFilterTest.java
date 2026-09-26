package com.nexum.commons.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitFilterTest {
    private static final String LOGIN = "/api/v1/auth/login";

    private final RateLimitFilter filter = new RateLimitFilter(new RateLimitProperties(
            true, "/api/v1/auth/", List.of("/api/v1/auth/change-password"), LOGIN, 10, 3, 60));

    @Test
    void limitsLoginPerEmailEvenWhenTheIpChanges() throws Exception {
        for (int i = 1; i <= 3; i++) {
            assertEquals(200, post(filter, LOGIN, "10.0.0." + i, "{\"email\":\"Ana@Example.com\",\"password\":\"x\"}"));
        }
        assertEquals(429, post(filter, LOGIN, "10.0.0.99", "{\"email\":\"ana@example.com\",\"password\":\"x\"}"));
    }

    @Test
    void limitsPerIpAndPath() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertEquals(200, post(filter, "/api/v1/auth/register", "10.1.1.1", "{}"));
        }
        assertEquals(429, post(filter, "/api/v1/auth/register", "10.1.1.1", "{}"));
        assertEquals(200, post(filter, "/api/v1/auth/register", "10.1.1.2", "{}"));
    }

    @Test
    void ignoresExcludedPaths() throws Exception {
        for (int i = 0; i < 20; i++) {
            assertEquals(200, post(filter, "/api/v1/auth/change-password", "10.2.2.2", "{}"));
        }
    }

    @Test
    void theProtectedPrefixIsConfigurable() throws Exception {
        RateLimitFilter v2 = new RateLimitFilter(new RateLimitProperties(
                true, "/api/v2/auth/", List.of(), "/api/v2/auth/login", 1, 1, 60));
        assertEquals(200, post(v2, "/api/v2/auth/register", "10.4.4.4", "{}"));
        assertEquals(429, post(v2, "/api/v2/auth/register", "10.4.4.4", "{}"));
        assertEquals(200, post(v2, "/api/v1/auth/register", "10.4.4.4", "{}"));
        assertEquals(200, post(v2, "/api/v1/auth/register", "10.4.4.4", "{}"));
    }

    @Test
    void rejectionUsesTheStandardErrorBody() throws Exception {
        RateLimitFilter strict = new RateLimitFilter(new RateLimitProperties(
                true, "/api/v1/auth/", List.of(), LOGIN, 0, 0, 30));
        MockHttpServletResponse response = new MockHttpServletResponse();
        strict.doFilter(request("/api/v1/auth/register", "10.5.5.5", "{}"), response, new MockFilterChain());

        assertEquals(429, response.getStatus());
        assertEquals("30", response.getHeader("Retry-After"));
        assertTrue(response.getContentAsString().contains("\"code\":\"TOO_MANY_REQUESTS\""));
    }

    @Test
    void usesThePathWithoutContextPathRegardlessOfServletMapping() throws Exception {
        RateLimitFilter strict = new RateLimitFilter(new RateLimitProperties(
                true, "/api/v1/auth/", List.of(), LOGIN, 0, 0, 60));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/app/api/v1/auth/register");
        request.setContextPath("/app");
        request.setServletPath("/api");
        MockHttpServletResponse response = new MockHttpServletResponse();
        strict.doFilter(request, response, new MockFilterChain());
        assertEquals(429, response.getStatus());
    }

    @Test
    void loginBodyStillReachesTheControllerAfterBeingRead() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request(LOGIN, "10.3.3.3", "{\"email\":\"a@b.com\"}"), new MockHttpServletResponse(), chain);
        String seen = new String(chain.getRequest().getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("{\"email\":\"a@b.com\"}", seen);
    }

    private int post(RateLimitFilter target, String path, String ip, String body) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        target.doFilter(request(path, ip, body), response, new MockFilterChain());
        return response.getStatus();
    }

    private MockHttpServletRequest request(String path, String ip, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setServletPath(path);
        request.setRemoteAddr(ip);
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }
}
