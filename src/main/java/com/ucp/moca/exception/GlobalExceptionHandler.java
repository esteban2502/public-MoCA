package com.ucp.moca.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.dao.DataIntegrityViolationException;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateQuestionOrderException.class)
    public ResponseEntity<String> handleDuplicateQuestionOrder(DuplicateQuestionOrderException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler({
            DataIntegrityViolationException.class,
            ConstraintViolationException.class,
            SQLIntegrityConstraintViolationException.class
    })
    public ResponseEntity<String> handleDataIntegrityViolation(Exception ex) {
        String message = "No se puede completar la operación porque el registro está relacionado con otros datos.";
        String errorMessage = ex.getMessage();

        if (ex instanceof DataIntegrityViolationException dataIntegrityViolationException
                && dataIntegrityViolationException.getMostSpecificCause() != null
                && dataIntegrityViolationException.getMostSpecificCause().getMessage() != null) {
            errorMessage = dataIntegrityViolationException.getMostSpecificCause().getMessage();
        }

        if (errorMessage != null && errorMessage.toLowerCase().contains("foreign key")) {
            message = "No se puede eliminar la pregunta porque ya fue utilizada en una evaluación.";
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }
}
