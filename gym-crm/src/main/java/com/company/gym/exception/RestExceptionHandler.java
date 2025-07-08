package com.company.gym.exception;

import com.company.gym.dto.response.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    private static final String UNAUTHORIZED_MESSAGE = "Authentication failed or user is inactive. Please check your credentials.";
    private static final String NOT_FOUND_MESSAGE = "The requested resource could not be found.";
    private static final String BAD_REQUEST_MESSAGE = "The request is invalid. Please check the provided input.";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "An unexpected internal server error has occurred. Please try again later.";

    @ExceptionHandler({InvalidCredentialsException.class, InactiveUserException.class})
    protected ResponseEntity<ApiErrorResponse> handleUnauthorizedExceptions(RuntimeException ex) {
        log.warn("Authentication failure: {}", ex.getMessage());
        ApiErrorResponse apiError = new ApiErrorResponse(
                "Unauthorized",
                UNAUTHORIZED_MESSAGE
        );
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<ApiErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        ApiErrorResponse apiError = new ApiErrorResponse(
                "Not Found",
                NOT_FOUND_MESSAGE
        );
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({IllegalArgumentException.class, InvalidInputException.class})
    protected ResponseEntity<ApiErrorResponse> handleBadRequests(RuntimeException ex) {
        log.warn("Bad request due to invalid input or argument: {}", ex.getMessage());
        ApiErrorResponse apiError = new ApiErrorResponse(
                "Bad Request",
                BAD_REQUEST_MESSAGE
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ServiceException.class, Exception.class})
    protected ResponseEntity<ApiErrorResponse> handleInternalServerErrors(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        ApiErrorResponse apiError = new ApiErrorResponse(
                "Internal Server Error",
                INTERNAL_SERVER_ERROR_MESSAGE
        );
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
