package com.trading.catalog.advice;

import com.trading.catalog.application.domain.exception.NotFoundException;
import com.trading.catalog.application.domain.exception.ValidationException;
import com.trading.catalog.restapi.generated.model.ErrorResponseWebDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseWebDto handleNotFound(NotFoundException ex) {
        var dto = new ErrorResponseWebDto();
        dto.setError("NOT_FOUND");
        dto.setMessage(ex.getMessage());
        dto.setTimestamp(OffsetDateTime.now());
        return dto;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseWebDto handleDomainValidation(ValidationException ex) {
        var dto = new ErrorResponseWebDto();
        dto.setError("BAD_REQUEST");
        dto.setMessage(ex.getMessage());
        dto.setTimestamp(OffsetDateTime.now());
        return dto;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseWebDto handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        var dto = new ErrorResponseWebDto();
        dto.setError("BAD_REQUEST");
        dto.setMessage(message.isEmpty() ? "Validation failed" : message);
        dto.setTimestamp(OffsetDateTime.now());
        return dto;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseWebDto handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> lastNode(violation.getPropertyPath().toString()) + " " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return badRequest(message.isEmpty() ? "Validation failed" : message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseWebDto handleMissingParameter(MissingServletRequestParameterException ex) {
        return badRequest(ex.getParameterName() + " is required");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseWebDto handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return badRequest(ex.getName() + " has an invalid value");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseWebDto handleNoResource(NoResourceFoundException ex) {
        var dto = new ErrorResponseWebDto();
        dto.setError("NOT_FOUND");
        dto.setMessage("No endpoint " + ex.getHttpMethod() + " /" + ex.getResourcePath());
        dto.setTimestamp(OffsetDateTime.now());
        return dto;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseWebDto handleUnexpected(Exception ex) {
        var dto = new ErrorResponseWebDto();
        dto.setError("INTERNAL_SERVER_ERROR");
        dto.setMessage("An unexpected error occurred");
        dto.setTimestamp(OffsetDateTime.now());
        return dto;
    }

    private static ErrorResponseWebDto badRequest(String message) {
        var dto = new ErrorResponseWebDto();
        dto.setError("BAD_REQUEST");
        dto.setMessage(message);
        dto.setTimestamp(OffsetDateTime.now());
        return dto;
    }

    private static String lastNode(String propertyPath) {
        return propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
    }
}
