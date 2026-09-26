package com.nexum.commons.error;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Escribe un {@link ErrorDTO} directamente en la respuesta, para los puntos donde no actúa el
 * {@link GlobalExceptionHandler}: filtros (JWT, rate limit) y los handlers de Spring Security.
 */
public final class ErrorResponses {
    private ErrorResponses() {
    }

    public static void write(HttpServletResponse response, ErrorCode code, String message) throws IOException {
        int status = code.httpStatus().value();
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":\"" + escape(code.name()) + "\",\"message\":\"" + escape(message)
                + "\",\"statusCode\":" + status + ",\"type\":\"" + code.httpStatus().name() + "\",\"details\":null}");
    }

    // ponytail: escape JSON mínimo a mano para no depender de Jackson; cubre comillas, barra y control < 0x20
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}
