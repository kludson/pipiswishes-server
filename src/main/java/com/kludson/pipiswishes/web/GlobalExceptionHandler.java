package com.kludson.pipiswishes.web;

import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import org.slf4j.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.EntityNotFoundException;

@ControllerAdvice 
public class GlobalExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
        Exception e
    ) {
        logger.error("Handle exception: {}", e);

        var errorDto = new ErrorResponseDto(
            "InternalServerException",
            e.getMessage(),
            LocalDateTime.now());

        return ResponseEntity
            .status(500).body(errorDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFoundException(
        EntityNotFoundException e
    ) {
        logger.error("Handle EntityNotFoundException: {}", e);

        var errorDto = new ErrorResponseDto(
            "Entity not found",
            e.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity
        .status(404).body(errorDto);
    }

    @ExceptionHandler(exception = {
        IllegalArgumentException.class,
        IllegalStateException.class,
        MethodArgumentNotValidException.class
    })
    public ResponseEntity<ErrorResponseDto> handleIllegalException(
        Exception e
    ) {
        logger.error("Handle EntityNotFoundException: {}", e);

        var errorDto = new ErrorResponseDto(
            "Bad request",
            e.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity
        .status(400).body(errorDto);
    }
}
