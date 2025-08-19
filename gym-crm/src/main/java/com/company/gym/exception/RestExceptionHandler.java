package com.company.gym.exception;

import com.company.gym.dto.response.ApiErrorResponse;
import com.company.gym.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @Autowired
    private LoginAttemptService loginAttemptService;

    private static final String GENERIC_AUTH_FAILURE_MESSAGE = "Authentication failed. Please check your credentials or contact support if the issue persists.";
    private static final String NOT_FOUND_MESSAGE = "The requested resource could not be found.";
    private static final String BAD_REQUEST_MESSAGE = "The request is invalid. Please check the provided input.";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "An unexpected internal server error has occurred. Please try again later.";
    private static final String LOCKED_ERROR_MESSAGE = "Your IP has been temporarily blocked due to too many failed login attempts.";

    @ExceptionHandler({BadCredentialsException.class, InvalidCredentialsException.class})
    protected ResponseEntity<ApiErrorResponse> handleBadCredentials(Exception ex, HttpServletRequest request) {
        log.warn("Authentication failure: {}", ex.getMessage());

        ApiErrorResponse apiError = new ApiErrorResponse(
                "Unauthorized",
                GENERIC_AUTH_FAILURE_MESSAGE
        );
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler({DisabledException.class, InactiveUserException.class})
    protected ResponseEntity<ApiErrorResponse> handleDisabledAccount(Exception ex) {
        log.warn("Login attempt for disabled account: {}", ex.getMessage());
        ApiErrorResponse apiError = new ApiErrorResponse(
                "Forbidden",
                GENERIC_AUTH_FAILURE_MESSAGE
        );
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
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

    @ExceptionHandler({LockedException.class})
    protected ResponseEntity<ApiErrorResponse> handleLockedAccount(LockedException ex) {
        log.warn("Login attempt from a blocked IP: {}", ex.getMessage());
        ApiErrorResponse apiError = new ApiErrorResponse(
                "IP Blocked",
                LOCKED_ERROR_MESSAGE
        );
        return new ResponseEntity<>(apiError, HttpStatus.LOCKED);
    }

    private String getClientIP(HttpServletRequest request) {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
