package com.company.gym.controller;

import com.company.gym.dto.request.ChangePasswordRequest;
import com.company.gym.entity.Credentials;
import com.company.gym.exception.InvalidCredentialsException;
import com.company.gym.service.AuthenticationService;
import com.company.gym.service.TraineeService;
import com.company.gym.service.TrainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    @Autowired
    public AuthController(AuthenticationService authService, TraineeService traineeService, TrainerService trainerService) {
        this.authService = authService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(@RequestParam String username, @RequestParam String password) {
        try {
            authService.authenticate(new Credentials(username, password));
            logger.info("Login successful for user: {}", username);
            return ResponseEntity.ok().build();
        } catch (InvalidCredentialsException e) {
            logger.warn("Login failed for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            traineeService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
            logger.info("Password successfully changed for trainee: {}", request.getUsername());
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            logger.debug("Could not change password for user {} as a trainee. Trying as a trainer.", request.getUsername());
            try {
                trainerService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
                logger.info("Password successfully changed for trainer: {}", request.getUsername());
                return ResponseEntity.ok().build();
            } catch (Exception finalException) {
                logger.warn("Password change failed for user {}: {}", request.getUsername(), finalException.getMessage());
                return ResponseEntity.status(401).build();
            }
        }
    }
}
