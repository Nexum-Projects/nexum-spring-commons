package com.nexum.commons.error;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * Código de error de la API. Cada aplicación define los suyos con un enum que implemente esta interfaz:
 * {@code name()} es el código que recibe el cliente y {@link #httpStatus()} el estado de la respuesta.
 * Es {@link Serializable} porque viaja dentro de {@link BusinessException}; los enums ya lo cumplen.
 */
public interface ErrorCode extends Serializable {
    String name();

    HttpStatus httpStatus();
}
