package com.hubinterior.client.Exception;

import com.hubinterior.client.Exception.dto.ErrorResponse;
import com.hubinterior.client.Exception.dto.ValidationErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Handle Custom Base ApiException Hierarchy ──────────────────────────────
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ErrorResponse(
                        ex.getStatus().value(),
                        ex.getErrorCode(),
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    // ── 400 — Validation Failures (@Valid) ────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String field = ((FieldError) error).getField();
                    String message = error.getDefaultMessage();
                    errors.put(field, message);
                });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ErrorCode.VALIDATION_FAILED,
                        "Validation failed",
                        errors,
                        LocalDateTime.now()
                ));
    }

    // ── 400 — Invalid Arguments ───────────────────────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ErrorCode.VALIDATION_FAILED,
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    // ── 401 — Bad Credentials (Authentication Failure) ────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        ErrorCode.UNAUTHORIZED,
                        "Invalid username or password.",
                        LocalDateTime.now()
                ));
    }

    // ── 403 — Access Denied (Authorization Failure) ───────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        ErrorCode.FORBIDDEN,
                        "Access denied: You do not have permission to execute this operation.",
                        LocalDateTime.now()
                ));
    }

    // ── 409 — Data Integrity Violations (Database Unique Constraint / Keys) ────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Database error: A unique constraint or relational integrity rule was violated.";
        if (ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null) {
            String cause = ex.getMostSpecificCause().getMessage();
            if (cause.contains("Duplicate entry")) {
                message = "Duplicate entry error: An item with this unique key already exists.";
            }
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        ErrorCode.DUPLICATE_RESOURCE,
                        message,
                        LocalDateTime.now()
                ));
    }

    // ── 400 — Invalid Sort / Query Parameters ─────────────────────────────────
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSortProperty(PropertyReferenceException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ErrorCode.VALIDATION_FAILED,
                        "Invalid sort or filter property: " + ex.getPropertyName(),
                        LocalDateTime.now()
                ));
    }

    // ── 500 — Sanitized Catch-all for Unexpected Errors ───────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ErrorCode.INTERNAL_SERVER_ERROR,
                        "An unexpected internal error occurred. Please try again later.",
                        LocalDateTime.now()
                ));
    }
}
