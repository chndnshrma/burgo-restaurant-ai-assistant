package com.burgo.api.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.burgo.api.core.RuleViolation;

/** Every error the API returns has the same shape: { message, fieldErrors }. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<Dto.ApiError> business(BusinessException e) {
        return ResponseEntity.status(e.status()).body(new Dto.ApiError(e.getMessage(), Map.of()));
    }

    @ExceptionHandler(RuleViolation.class)
    ResponseEntity<Dto.ApiError> rule(RuleViolation e) {
        return ResponseEntity.badRequest().body(new Dto.ApiError(e.getMessage(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Dto.ApiError> invalid(MethodArgumentNotValidException e) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fe -> fields.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new Dto.ApiError("Please check the highlighted fields.", fields));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    ResponseEntity<Dto.ApiError> unreadable(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Dto.ApiError("We couldn't read that request. Please check the date, time and numbers.", Map.of()));
    }
}
