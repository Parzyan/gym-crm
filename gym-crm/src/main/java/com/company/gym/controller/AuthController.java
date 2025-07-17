package com.company.gym.controller;

import com.company.gym.dto.request.AuthenticationRequest;
import com.company.gym.dto.request.ChangePasswordRequest;
import com.company.gym.dto.response.AuthenticationResponse;
import com.company.gym.security.JwtUtil;
import com.company.gym.service.LoginAttemptService;
import com.company.gym.service.TraineeService;
import com.company.gym.service.TrainerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Authentication", description = "Operations for user login and password management")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final LoginAttemptService loginAttemptService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, TraineeService traineeService, TrainerService trainerService, LoginAttemptService loginAttemptService, JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.loginAttemptService = loginAttemptService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Operation(summary = "Authenticate user and get JWT", description = "Provides a JWT token for valid credentials.")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest,
                                                                            HttpServletRequest request) {
        final String clientIp = getClientIP(request);
        if (loginAttemptService.isBlocked(clientIp)) {
            throw new LockedException("Your IP has been temporarily blocked due to too many failed login attempts.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
        );

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

    @Operation(summary = "Change a user's login password", description = "Allows a user to change their password after providing the old one.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Incorrect old password or user not found")
    })
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            traineeService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
            logger.info("Password successfully changed for trainee: {}", request.getUsername());
        } catch (SecurityException e) {
            logger.debug("Could not change password for user {} as a trainee. Trying as a trainer.", request.getUsername());

            trainerService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
            logger.info("Password successfully changed for trainer: {}", request.getUsername());
        }
        return ResponseEntity.ok().build();
    }

    private String getClientIP(HttpServletRequest request) {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
