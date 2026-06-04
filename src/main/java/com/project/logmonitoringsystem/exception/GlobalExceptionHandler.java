package com.project.logmonitoringsystem.exception;

import com.project.logmonitoringsystem.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleException(Exception ex) {

       ErrorResponseDTO error = new ErrorResponseDTO(
               ex.getMessage(),
               HttpStatus.INTERNAL_SERVER_ERROR.value(),
               HttpStatus.NOT_FOUND.getReasonPhrase(),
               LocalDateTime.now()
       );

       return new ResponseEntity<>(
               error,
               HttpStatus.INTERNAL_SERVER_ERROR
       );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex) {

        ErrorResponseDTO error =
                new ErrorResponseDTO(
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        LocalDateTime.now()
                );

        return new ResponseEntity<>(
                error,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ResourceNotFoundException.InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidToken(ResourceNotFoundException.InvalidTokenException ex) {

        ErrorResponseDTO error =
                new ErrorResponseDTO(
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        LocalDateTime.now()
                );

        return new ResponseEntity<>(
                error,
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(ResourceNotFoundException.BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(ResourceNotFoundException.BadRequestException ex) {

        ErrorResponseDTO error =
                new ErrorResponseDTO(
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        LocalDateTime.now()
                );

        return new ResponseEntity<>(
                error,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        ));

        return ResponseEntity.badRequest().body(errors);
    }
}
