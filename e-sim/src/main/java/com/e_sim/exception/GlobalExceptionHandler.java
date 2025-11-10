package com.e_sim.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
// import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;

import com.e_sim.dto.response.ApiRes;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiRes<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", errorMessage);

        return ResponseEntity.badRequest()
                .body(ApiRes.error(errorMessage));
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ApiRes<String>> handleAuthenticationException(Exception ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiRes.error("Invalid credentials"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiRes<String>> handleGeneralException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                .body(ApiRes.error("An unexpected error occurred"));
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<ApiRes<String>> handleMethodNotAllowedException(MethodNotAllowedException ex) {

        var response = ApiRes.error(ex.getLocalizedMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiRes<String>> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {

        var response = ApiRes.error(ex.getLocalizedMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiRes<String>> handleConstraintViolationException(ConstraintViolationException ex) {

        var response = ApiRes.error(ex.getLocalizedMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiRes<String>> handleSQLException(SQLException ex) {
        if (ex.getErrorCode() == 1062) { // MySQL duplicate entry error code
            var response = ApiRes.error("constraint violation");
            return ResponseEntity.internalServerError().body(response);
        }
        var response = ApiRes.error(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BatchUpdateException.class)
    public ResponseEntity<ApiRes<String>> handleBatchUpdateException(BatchUpdateException ex) {

        var response = ApiRes.error(ex.getLocalizedMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ApiRes<String>> handlePersistenceException(PersistenceException ex) {

        var response = ApiRes.error(ex.getLocalizedMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiRes<String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {

        var response = ApiRes.error(ex.getLocalizedMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiRes<String>> handleDataAccessException(DataAccessException ex) {
        log.error("Database error occurred: {}", ex.getMessage(), ex);

        String errorMessage = (ex.getRootCause() instanceof SQLTimeoutException)
                ? "Database connection timed out"
                : "Error accessing database";

        var response = ApiRes.error(errorMessage);
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(org.hibernate.exception.ConstraintViolationException.class)
    public ResponseEntity<ApiRes<String>> handleConstraintViolationException(org.hibernate.exception.ConstraintViolationException ex) {

        var response = ApiRes.error(ex.getLocalizedMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiRes<String>> handleBusinessRuleException(BusinessRuleException ex) {

        var response = ApiRes.error(ex.getLocalizedMessage());
        return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiRes<String>> handleDuplicateResourceException(DuplicateResourceException ex) {

        var response = ApiRes.error(ex.getLocalizedMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiRes<String>> handleEntityNotFoundException(EntityNotFoundException ex) {

        var response = ApiRes.error(ex.getLocalizedMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ApiRes<String>> handleAccessDeniedException(AccessDeniedException ex) {

        var response = ApiRes.error(ex.getLocalizedMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}
