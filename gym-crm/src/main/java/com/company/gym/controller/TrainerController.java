package com.company.gym.controller;

import com.company.gym.dto.request.TrainerRegistrationRequest;
import com.company.gym.dto.request.UpdateActiveStatusRequest;
import com.company.gym.dto.request.UpdateTrainerProfileRequest;
import com.company.gym.dto.response.TrainerProfileResponse;
import com.company.gym.dto.response.UserCredentialsResponse;
import com.company.gym.entity.Credentials;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.Training;
import com.company.gym.exception.EntityNotFoundException;
import com.company.gym.service.TrainerService;
import jakarta.validation.Valid;
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
import io.micrometer.core.instrument.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Trainer Management", description = "Endpoints for creating, updating, and retrieving trainer data")
@RestController
@RequestMapping("/trainers")
public class TrainerController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerController.class);
    private final TrainerService trainerService;

    private final Counter registrationCounter;
    private final Timer profileUpdateTimer;
    private final DistributionSummary traineesPerTrainer;
    private final Counter statusUpdateCounter;

    @Autowired
    public TrainerController(TrainerService trainerService, MeterRegistry meterRegistry) {
        this.trainerService = trainerService;

        this.registrationCounter = Counter.builder("trainer.registrations.total")
                .description("Total number of new trainer registrations")
                .tag("entity", "trainer")
                .register(meterRegistry);

        this.profileUpdateTimer = Timer.builder("trainer.profile.updates.latency")
                .description("Time taken to update a trainer's profile")
                .publishPercentiles(0.5, 0.95)
                .register(meterRegistry);

        this.traineesPerTrainer = DistributionSummary.builder("trainer.assigned.trainees.distribution")
                .description("Distribution of the number of trainees assigned to a trainer")
                .baseUnit("trainees")
                .register(meterRegistry);

        this.statusUpdateCounter = Counter.builder("trainer.status.updates.total")
                .description("Total number of trainer status updates (activate/deactivate)")
                .register(meterRegistry);
    }

    @Operation(summary = "Register a new trainer", description = "Creates a new trainer profile and returns their generated credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainer created successfully",
                    content = @Content(schema = @Schema(implementation = UserCredentialsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data")
    })
    @PostMapping("/register")
    public ResponseEntity<UserCredentialsResponse> registerTrainer(@Valid @RequestBody TrainerRegistrationRequest request) {
        UserCredentialsResponse response = trainerService.createTrainerProfile(
                request.getFirstName(), request.getLastName(), request.getSpecializationId());

        registrationCounter.increment();

        logger.info("Successfully registered trainer. Username: {}", response.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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
            Principal principal) {
        String username = principal.getName();
        Trainer trainer = trainerService.getByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer with username '" + username + "' not found."));

        TrainerProfileResponse response = mapTrainerToProfileResponse(trainer);
        traineesPerTrainer.record(response.getTrainees().size());

        logger.info("Successfully fetched profile for trainer: {}", username);
        return ResponseEntity.ok(mapTrainerToProfileResponse(trainer));
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
            Principal principal,
            @Valid @RequestBody UpdateTrainerProfileRequest request) {
        String username = principal.getName();
        return profileUpdateTimer.record(() -> {
            Trainer currentTrainer = trainerService.getByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Trainer with username '" + username + "' not found."));

            if (currentTrainer.getUser().getIsActive() != request.isActive()) {
                trainerService.updateStatus(new Credentials(username, null));
            }

            Trainer updatedTrainer = trainerService.getByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Trainer with username '" + username + "' disappeared after update."));

            TrainerProfileResponse response = mapTrainerToProfileResponse(updatedTrainer);

            logger.info("Successfully updated trainer profile: {}", username);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Activate or deactivate a trainer", description = "Sets the active status for a trainer's profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainer does not exist"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials")
    })
    @PatchMapping("/status")
    public ResponseEntity<Void> updateTrainerStatus(@Valid @RequestBody UpdateActiveStatusRequest request) {
        Trainer trainer = trainerService.getByUsername(request.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainer with username '" + request.getUsername() + "' not found."));

        if (trainer.getUser().getIsActive() != request.isActive()) {
            Credentials credentials = new Credentials(request.getUsername(), request.getPassword());
            trainerService.updateStatus(credentials);

            statusUpdateCounter.increment();
        }

        logger.info("Successfully updated status for trainer {}", request.getUsername());
        return ResponseEntity.ok().build();
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
