package com.nexum.commons.error;

import org.springframework.http.HttpStatus;

/** Códigos genéricos que la librería usa por sí misma. Los propios de cada aplicación van en su enum. */
public enum CommonErrorCode implements ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    BAD_REQUEST_VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    NOT_FOUND_RESOURCE(HttpStatus.NOT_FOUND),
    CONFLICT_DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT),
    CONFLICT_UNIQUE_CONSTRAINT_VIOLATION(HttpStatus.CONFLICT),
    CONFLICT_OPTIMISTIC_LOCK(HttpStatus.CONFLICT),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    CommonErrorCode(HttpStatus status) {
        this.status = status;
    }

    @Override
    public HttpStatus httpStatus() {
        return status;
    }
}
