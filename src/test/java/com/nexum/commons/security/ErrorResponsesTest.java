package com.nexum.commons.security;

import com.nexum.commons.error.CommonErrorCode;
import com.nexum.commons.error.ErrorResponses;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorResponsesTest {
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void writesTheStandardErrorBodyAndEscapesTheMessage() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ErrorResponses.write(response, CommonErrorCode.UNAUTHORIZED, "Token \"expirado\"\nvuelva a entrar \\ ya");

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        JsonNode body = json.readTree(response.getContentAsString());
        assertEquals("UNAUTHORIZED", body.get("code").asString());
        assertEquals("Token \"expirado\"\nvuelva a entrar \\ ya", body.get("message").asString());
        assertEquals(401, body.get("statusCode").asInt());
        assertEquals("UNAUTHORIZED", body.get("type").asString());
        assertTrue(body.get("details").isNull());
    }

    @Test
    void entryPointAnswers401() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new RestAuthenticationEntryPoint().commence(new MockHttpServletRequest(), response, new BadCredentialsException("x"));

        assertEquals(401, response.getStatus());
        assertEquals("UNAUTHORIZED", json.readTree(response.getContentAsString()).get("code").asString());
    }

    @Test
    void accessDeniedHandlerAnswers403() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new RestAccessDeniedHandler().handle(new MockHttpServletRequest(), response, new AccessDeniedException("x"));

        assertEquals(403, response.getStatus());
        assertEquals("FORBIDDEN", json.readTree(response.getContentAsString()).get("code").asString());
        assertEquals("Access denied", json.readTree(response.getContentAsString()).get("message").asString());
    }
}
