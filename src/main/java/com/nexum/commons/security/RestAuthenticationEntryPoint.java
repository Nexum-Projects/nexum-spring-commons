package com.nexum.commons.security;

import com.nexum.commons.error.CommonErrorCode;
import com.nexum.commons.error.ErrorResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/** 401 con el {@code ErrorDTO} estándar cuando falta autenticación. Registrarlo en el {@code SecurityFilterChain}. */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        ErrorResponses.write(response, CommonErrorCode.UNAUTHORIZED,
                "Authentication required. Please provide a valid Bearer token.");
    }
}
