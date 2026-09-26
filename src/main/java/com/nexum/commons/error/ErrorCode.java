package com.nexum.commons.error;

import org.springframework.http.HttpStatus;

/**
 * Código de error de la API. Cada aplicación define los suyos con un enum que implemente esta interfaz:
 * {@code name()} es el código que recibe el cliente y {@link #httpStatus()} el estado de la respuesta.
 */
public interface ErrorCode {
    String name();

    HttpStatus httpStatus();
}
