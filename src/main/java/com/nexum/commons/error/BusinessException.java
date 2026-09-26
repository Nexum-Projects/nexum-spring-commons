package com.nexum.commons.error;

/** Error de negocio. El {@link ErrorCode} decide el estado HTTP y el código que ve el cliente. */
public class BusinessException extends RuntimeException {
    private final ErrorCode code;

    public BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
