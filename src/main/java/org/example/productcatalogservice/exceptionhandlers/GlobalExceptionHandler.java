package org.example.productcatalogservice.exceptionhandlers;

import org.example.productcatalogservice.dtos.ErrorResponseDto;
import org.example.productcatalogservice.exceptions.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException exception) {

        // 1. Package the thrown message into our DTO
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(exception.getMessage());

        // 2. Return the DTO with a 400 Bad Request status
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleProductNotFoundException(ProductNotFoundException exception) {

        // 1. Package the custom message into our standard DTO
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(exception.getMessage());

        // 2. Return the DTO with a 404 Not Found status
        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException exception) {

        // Return a clean, user-friendly message instead of the raw stack trace
        ErrorResponseDto errorResponse = new ErrorResponseDto("The requested URL path does not exist.");

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

}