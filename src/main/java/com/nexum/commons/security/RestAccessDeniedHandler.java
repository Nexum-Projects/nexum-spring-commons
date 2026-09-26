package com.nexum.commons.security;

import com.nexum.commons.error.CommonErrorCode;
import com.nexum.commons.error.ErrorResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/** 403 con el {@code ErrorDTO} estándar cuando el usuario autenticado no tiene permiso. */
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        ErrorResponses.write(response, CommonErrorCode.FORBIDDEN, "Access denied");
    }
}
