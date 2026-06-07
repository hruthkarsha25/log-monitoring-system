package com.project.logmonitoringsystem.exception;

import com.project.logmonitoringsystem.dto.ErrorResponseDTO;
import com.project.logmonitoringsystem.enums.LogLevel;
import com.project.logmonitoringsystem.service.EventLoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final EventLoggingService eventLoggingService;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleException(Exception ex) {

        eventLoggingService.log(
                "system",
                LogLevel.ERROR,
                "SYSTEM_ERROR",
                null,
                null,
                null
        );

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

        eventLoggingService.log(
                "auth-service",
                LogLevel.WARN,
                "JWT_INVALID",
                null,
                null,
                null
        );

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

        eventLoggingService.log(
                "system",
                LogLevel.WARN,
                "BAD_REQUEST",
                null,
                null,
                null
        );

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

        eventLoggingService.log(
                "system",
                LogLevel.WARN,
                "VALIDATION_FAILED",
                null,
                null,
                null
        );

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
