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
import com.company.gym.service.TrainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
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
public class TrainerController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerController.class);
    private final TrainerService trainerService;

    @Autowired
    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
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

    @Operation(summary = "Get a trainer's profile", description = "Retrieves the full profile details for a single trainer by their username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved trainer profile",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainer does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @RequestAttribute("authenticatedUsername") String username) {
        try {
            Trainer trainer = trainerService.getByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Trainer not found in database: " + username));

            return ResponseEntity.ok(mapTrainerToProfileResponse(trainer));
        } catch (IllegalStateException e) {
            logger.error("Error retrieving trainer profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Update a trainer's profile", description = "Updates the profile information for an existing trainer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated trainer profile",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainer does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PutMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            @RequestAttribute("authenticatedUsername") String username,
            @RequestBody UpdateTrainerProfileRequest request) {
        try {
            Trainer currentTrainer = trainerService.getByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

            if (currentTrainer.getUser().getIsActive() != request.isActive()) {
                trainerService.updateStatus(new Credentials(username, null));
            }

            Trainer updatedTrainer = trainerService.getByUsername(username).get();
            TrainerProfileResponse response = mapTrainerToProfileResponse(updatedTrainer);

            logger.info("Successfully updated trainer profile: {}", username);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Update profile failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
