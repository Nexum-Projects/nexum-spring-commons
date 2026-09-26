package com.nexum.commons.error;

import java.util.Map;

/** Cuerpo de toda respuesta de error. El formato es un contrato estable con los clientes. */
public record ErrorDTO(String code, String message, int statusCode, String type, Map<String, Object> details) {
}
