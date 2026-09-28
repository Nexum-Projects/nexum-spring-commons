package com.nexum.commons.error;

import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /** Un código propio de una aplicación: el estado HTTP viaja con el código. */
    enum PurchaseErrorCode implements ErrorCode {
        PURCHASE_ALREADY_CONFIRMED(HttpStatus.CONFLICT);

        private final HttpStatus status;

        PurchaseErrorCode(HttpStatus status) {
            this.status = status;
        }

        @Override
        public HttpStatus httpStatus() {
            return status;
        }
    }

    @Test
    void aProjectSpecificCodeCarriesItsOwnStatus() {
        ResponseEntity<ErrorDTO> response = handler.handleBusiness(
                new BusinessException(PurchaseErrorCode.PURCHASE_ALREADY_CONFIRMED, "already confirmed"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(new ErrorDTO("PURCHASE_ALREADY_CONFIRMED", "already confirmed", 409, "CONFLICT", null),
                response.getBody());
    }

    @Test
    void notFoundMapsTo404() {
        ResponseEntity<ErrorDTO> response = handler.handleBusiness(new NotFoundException("missing"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOT_FOUND_RESOURCE", response.getBody().code());
        assertEquals("missing", response.getBody().message());
    }

    @Test
    void validationListsEachFieldEvenWhenAMessageIsMissing() throws Exception {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "request");
        errors.addError(new FieldError("request", "name", "name is required"));
        errors.addError(new FieldError("request", "email", null, false, null, null, null));
        MethodParameter parameter = new MethodParameter(Object.class.getMethod("equals", Object.class), 0);

        ResponseEntity<ErrorDTO> response =
                handler.handleValidation(new MethodArgumentNotValidException(parameter, errors));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST_VALIDATION_ERROR", response.getBody().code());
        assertEquals(Map.of("name", "name is required", "email", "invalid"),
                response.getBody().details().get("fieldErrors"));
    }

    /** Parámetros de consulta: el setter valida y lanza IllegalArgumentException. */
    public static class Params {
        private String orderBy;
        private Integer page;

        public String getOrderBy() {
            return orderBy;
        }

        public void setOrderBy(String orderBy) {
            throw new IllegalArgumentException("Invalid orderBy field: " + orderBy);
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }
    }

    @Test
    void bindingErrorsExposeTheCauseMessageNotInternalClassNames() throws Exception {
        DataBinder binder = new DataBinder(new Params(), "params");
        binder.bind(new MutablePropertyValues(Map.of("orderBy", "password", "page", "abc")));
        MethodParameter parameter = new MethodParameter(Object.class.getMethod("equals", Object.class), 0);

        ResponseEntity<ErrorDTO> response = handler.handleValidation(
                new MethodArgumentNotValidException(parameter, binder.getBindingResult()));

        Map<?, ?> fieldErrors = (Map<?, ?>) response.getBody().details().get("fieldErrors");
        assertEquals("Invalid orderBy field: password", fieldErrors.get("orderBy"));
        assertEquals("invalid value", fieldErrors.get("page"));
    }

    @Test
    void uniqueViolationIsDistinguishedFromOtherIntegrityErrors() {
        ResponseEntity<ErrorDTO> unique = handler.handleDataIntegrity(
                new DataIntegrityViolationException("dup", new SQLException("duplicate", "23505")));
        ResponseEntity<ErrorDTO> other = handler.handleDataIntegrity(
                new DataIntegrityViolationException("fk", new SQLException("fk", "23503")));

        assertEquals("CONFLICT_UNIQUE_CONSTRAINT_VIOLATION", unique.getBody().code());
        assertEquals("CONFLICT_DATA_INTEGRITY_VIOLATION", other.getBody().code());
        assertEquals(HttpStatus.CONFLICT, other.getStatusCode());
    }

    @Test
    void optimisticLockMapsTo409() {
        ResponseEntity<ErrorDTO> response = handler.handleOptimisticLock(new OptimisticLockingFailureException("stale"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT_OPTIMISTIC_LOCK", response.getBody().code());
    }

    @Test
    void unreadableBodyMapsTo400WithoutInternalDetails() {
        ResponseEntity<ErrorDTO> response = handler.handleUnreadableBody(new HttpMessageNotReadableException(
                "Cannot deserialize value of type `Level` from String \"ADMIN\"",
                new MockHttpInputMessage(new byte[0])));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(new ErrorDTO("BAD_REQUEST", "Malformed request body", 400, "BAD_REQUEST", null), response.getBody());
    }

    @Test
    void unexpectedErrorDoesNotLeakItsMessage() {
        ResponseEntity<ErrorDTO> response = handler.handleGeneric(new RuntimeException("secret detail"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }
}
