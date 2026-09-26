package com.nexum.commons.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Convierte las excepciones en {@link ErrorDTO}. Con prioridad mínima: un {@code @RestControllerAdvice}
 * propio de la aplicación gana sobre este.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDTO> handleBusiness(BusinessException ex) {
        return build(ex.getCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Objects.requireNonNullElse(error.getDefaultMessage(), "invalid"),
                        (first, second) -> first));
        return build(CommonErrorCode.BAD_REQUEST_VALIDATION_ERROR, "Request validation failed",
                Map.of("fieldErrors", fieldErrors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDTO> handleDataIntegrity(DataIntegrityViolationException ex) {
        // SQLState 23505 = unique_violation
        boolean unique = NestedExceptionUtils.getMostSpecificCause(ex) instanceof SQLException sql
                && "23505".equals(sql.getSQLState());
        return unique
                ? build(CommonErrorCode.CONFLICT_UNIQUE_CONSTRAINT_VIOLATION, "Unique constraint violation on a field", null)
                : build(CommonErrorCode.CONFLICT_DATA_INTEGRITY_VIOLATION, "Data integrity violation", null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorDTO> handleOptimisticLock(OptimisticLockingFailureException ex) {
        return build(CommonErrorCode.CONFLICT_OPTIMISTIC_LOCK,
                "The resource was modified by someone else. Reload it and try again", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDTO> handleAccessDenied(AccessDeniedException ex) {
        return build(CommonErrorCode.FORBIDDEN, "Access denied", null);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorDTO> handleEndpointNotFound(Exception ex) {
        return build(CommonErrorCode.NOT_FOUND_RESOURCE, "Endpoint not found", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return build(CommonErrorCode.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(CommonErrorCode.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null);
    }

    private static ResponseEntity<ErrorDTO> build(ErrorCode code, String message, Map<String, Object> details) {
        HttpStatus status = code.httpStatus();
        return ResponseEntity.status(status)
                .body(new ErrorDTO(code.name(), message, status.value(), status.name(), details));
    }
}
