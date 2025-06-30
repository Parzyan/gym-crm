package com.company.gym.controller;

import com.company.gym.dto.request.TrainerRegistrationRequest;
import com.company.gym.dto.request.UpdateActiveStatusRequest;
import com.company.gym.dto.request.UpdateTrainerProfileRequest;
import com.company.gym.dto.response.TrainerProfileResponse;
import com.company.gym.dto.response.UserCredentialsResponse;
import com.company.gym.entity.Credentials;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.Training;
import com.company.gym.exception.InvalidCredentialsException;
import com.company.gym.service.AuthenticationService;
import com.company.gym.service.TrainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Trainer Management", description = "Endpoints for creating, updating, and retrieving trainer data")
@RestController
@RequestMapping("/trainers")
public class TrainerRestController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerRestController.class);
    private final TrainerService trainerService;
    private final AuthenticationService authenticationService;

    @Autowired
    public TrainerRestController(TrainerService trainerService, AuthenticationService authenticationService) {
        this.trainerService = trainerService;
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Register a new trainer", description = "Creates a new trainer profile and returns their generated credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainer created successfully",
                    content = @Content(schema = @Schema(implementation = UserCredentialsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data")
    })
    @PostMapping("/register")
    public ResponseEntity<UserCredentialsResponse> registerTrainer(@RequestBody TrainerRegistrationRequest request) {
        try {
            Trainer newTrainer = trainerService.createTrainerProfile(
                    request.getFirstName(), request.getLastName(), request.getSpecializationId());

            UserCredentialsResponse response = new UserCredentialsResponse(
                    newTrainer.getUser().getUsername(), newTrainer.getUser().getPassword());

            logger.info("Successfully registered trainer. Username: {}", response.getUsername());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error during trainer registration", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get a trainer's profile", description = "Retrieves the full profile details for a single trainer by their username. Requires credentials for authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved trainer profile",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials provided"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Authenticated user not found in database")
    })
    @GetMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @Parameter(description = "Username of the trainer to retrieve", required = true) @RequestParam String username,
            @Parameter(description = "Password of the trainer (for authentication)", required = true) @RequestParam String password) {

        try {
            authenticationService.authenticate(new Credentials(username, password));

            Trainer trainer = trainerService.getByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username));

            return ResponseEntity.ok(mapTrainerToProfileResponse(trainer));

        } catch (InvalidCredentialsException e) {
            logger.warn("Get profile failed for user '{}' due to invalid credentials.", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalStateException e) {
            logger.error("Data consistency error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update a trainer's profile", description = "Updates the profile information for an existing trainer. Note that the service layer for this operation only supports updating the 'isActive' status. First name, last name, and specialization are ignored.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated trainer profile",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication failure"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainer does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PutMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(@RequestBody UpdateTrainerProfileRequest request) {
        try {
            Trainer currentTrainer = trainerService.getByUsername(request.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

            if (currentTrainer.getUser().getIsActive() != request.isActive()) {
                Credentials credentials = new Credentials(request.getUsername(), request.getPassword());
                trainerService.updateStatus(credentials);
            }

            Trainer updatedTrainer = trainerService.getByUsername(request.getUsername()).get();
            TrainerProfileResponse response = mapTrainerToProfileResponse(updatedTrainer);

            logger.info("Successfully processed update for trainer profile: {}", request.getUsername());
            return ResponseEntity.ok(response);

        } catch (InvalidCredentialsException e) {
            logger.error("Update profile failed for user '{}' due to invalid credentials.", request.getUsername());
            return ResponseEntity.status(401).build();
        } catch (IllegalArgumentException e) {
            logger.error("Update profile failed for {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("An unexpected error occurred updating trainer {}", request.getUsername(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Activate or deactivate a trainer", description = "Sets the active status for a trainer's profile. Requires credentials in the body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Trainer not found with the given username"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials provided")
    })
    @PatchMapping("/status")
    public ResponseEntity<Void> updateTrainerStatus(@RequestBody UpdateActiveStatusRequest request) {
        try {
            Trainer trainer = trainerService.getByUsername(request.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Trainer not found with username: " + request.getUsername()));

            if (trainer.getUser().getIsActive() != request.isActive()) {
                Credentials credentials = new Credentials(request.getUsername(), request.getPassword());
                trainerService.updateStatus(credentials);
            }

            logger.info("Successfully updated status for trainer {}", request.getUsername());
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update status for trainer '{}': {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (InvalidCredentialsException e) {
            logger.warn("Authentication failed during status update for trainer '{}': {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private TrainerProfileResponse mapTrainerToProfileResponse(Trainer trainer) {
        TrainerProfileResponse response = new TrainerProfileResponse();
        response.setFirstName(trainer.getUser().getFirstName());
        response.setLastName(trainer.getUser().getLastName());
        response.setSpecialization(trainer.getSpecialization().getTrainingTypeName());
        response.setActive(trainer.getUser().getIsActive());

        List<TrainerProfileResponse.TraineeInfo> traineesFromTrainings = trainer.getTrainings()
                .stream()
                .map(Training::getTrainee)
                .distinct()
                .map(trainee -> new TrainerProfileResponse.TraineeInfo(
                        trainee.getUser().getUsername(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName()
                ))
                .collect(Collectors.toList());

        response.setTrainees(traineesFromTrainings);

        return response;
    }
}
