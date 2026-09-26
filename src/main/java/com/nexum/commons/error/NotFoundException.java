package com.nexum.commons.error;

/** Recurso inexistente: responde 404 con {@code NOT_FOUND_RESOURCE}. */
public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super(CommonErrorCode.NOT_FOUND_RESOURCE, message);
    }
}
