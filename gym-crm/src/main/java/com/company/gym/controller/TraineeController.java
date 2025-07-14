package com.company.gym.controller;

import com.company.gym.dto.request.*;
import com.company.gym.dto.response.TraineeProfileResponse;
import com.company.gym.dto.response.UnassignedTrainerResponse;
import com.company.gym.dto.response.UpdatedTrainersListResponse;
import com.company.gym.dto.response.UserCredentialsResponse;
import com.company.gym.entity.Credentials;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.Training;
import com.company.gym.exception.EntityNotFoundException;
import com.company.gym.service.TraineeService;
import com.company.gym.service.TrainerService;
import io.micrometer.core.instrument.MeterRegistry;
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

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Trainee Management", description = "Endpoints for creating, updating, and retrieving trainee data")
@RestController
@RequestMapping("/trainees")
public class TraineeController {
    private static final Logger logger = LoggerFactory.getLogger(TraineeController.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    private final Counter registrationCounter;
    private final Timer profileUpdateTimer;
    private final DistributionSummary trainersPerTrainee;
    private final Counter profileDeleteCounter;

    @Autowired
    public TraineeController(TraineeService traineeService, TrainerService trainerService, MeterRegistry meterRegistry) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;

        this.registrationCounter = Counter.builder("trainee.registrations.total")
                .description("Total number of new trainee registrations")
                .tag("entity", "trainee")
                .register(meterRegistry);

        this.profileUpdateTimer = Timer.builder("trainee.profile.updates.latency")
                .description("Time taken to update a trainee's profile")
                .publishPercentiles(0.5, 0.95)
                .register(meterRegistry);

        this.trainersPerTrainee = DistributionSummary.builder("trainee.trainers.count")
                .description("Distribution of the number of trainers assigned to a trainee")
                .baseUnit("trainers")
                .register(meterRegistry);

        this.profileDeleteCounter = Counter.builder("trainee.deletions.total")
                .description("Total number of trainee profiles deleted")
                .tag("entity", "trainee")
                .register(meterRegistry);
    }

    @Operation(summary = "Register a new trainee", description = "Creates a new trainee profile and returns their generated credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainee created successfully",
                    content = @Content(schema = @Schema(implementation = UserCredentialsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/register")
    public ResponseEntity<UserCredentialsResponse> registerTrainee(@Valid @RequestBody TraineeRegistrationRequest request) {
        Trainee newTrainee = traineeService.createTraineeProfile(
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth(),
                request.getAddress()
        );

        registrationCounter.increment();

        UserCredentialsResponse response = new UserCredentialsResponse(
                newTrainee.getUser().getUsername(),
                newTrainee.getUser().getPassword()
        );

        logger.info("Successfully registered trainee. Username: {}", response.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a trainee's profile", description = "Retrieves the full profile details for a single trainee by their username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved trainee profile",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainee not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/profile")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(
            @RequestAttribute("authenticatedUsername") String username) {
        Trainee trainee = traineeService.getByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with username '" + username + "' not found."));

        TraineeProfileResponse response = mapTraineeToProfileResponse(trainee);
        trainersPerTrainee.record(response.getTrainersList().size());

        logger.info("Successfully fetched profile for trainee: {}", username);
        return ResponseEntity.ok(mapTraineeToProfileResponse(trainee));
    }

    @Operation(summary = "Update a trainee's profile", description = "Updates the profile information for an existing trainee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated trainee profile",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainee does not exist")
    })
    @PutMapping("/profile")
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(
            @RequestAttribute("authenticatedUsername") String username,
            @Valid @RequestBody UpdateTraineeProfileRequest request) {

        return profileUpdateTimer.record(() -> {
            Trainee updatedTrainee = traineeService.updateTraineeProfile(
                    new Credentials(username, null),
                    request.getDateOfBirth(),
                    request.getAddress()
            );

            if (updatedTrainee.getUser().getIsActive() != request.isActive()) {
                traineeService.updateStatus(new Credentials(username, null));
            }

            Trainee finalTrainee = traineeService.getByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Trainee with username '" + username + "' not found after update."));

            TraineeProfileResponse response = mapTraineeToProfileResponse(finalTrainee);
            trainersPerTrainee.record(response.getTrainersList().size());

            logger.info("Successfully updated profile for trainee: {}", username);
            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Delete a trainee's profile", description = "Permanently deletes a trainee's profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Trainee profile deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainee does not exist")
    })
    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteTraineeProfile(
            @RequestAttribute("authenticatedUsername") String username) {
        traineeService.deleteTraineeProfile(new Credentials(username, null));

        profileDeleteCounter.increment();

        logger.info("Successfully deleted trainee profile: {}", username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get active trainers not assigned to a trainee", description = "Retrieves a list of trainers who are active and do not have any trainings with the specified trainee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of trainers"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainee does not exist")
    })
    @GetMapping("/unassigned-trainers")
    public ResponseEntity<List<UnassignedTrainerResponse>> getUnassignedTrainers(
            @RequestAttribute("authenticatedUsername") String username) {
        List<Trainer> unassignedTrainers = trainerService.getUnassignedTrainers(username);

        List<UnassignedTrainerResponse> response = unassignedTrainers.stream()
                .filter(trainer -> trainer.getUser().getIsActive())
                .map(trainer -> new UnassignedTrainerResponse(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization().getTrainingTypeName()
                ))
                .collect(Collectors.toList());

        logger.info("Successfully fetched unassigned, active trainers for trainee: {}", username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update multiple training trainers",
            description = "Update trainers for multiple trainings in a single request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated trainers",
                    content = @Content(schema = @Schema(implementation = UpdatedTrainersListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Not Found - Training or trainer not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Trainee doesn't own the training")
    })
    @PatchMapping("/trainers")
    public ResponseEntity<UpdatedTrainersListResponse> updateTrainingTrainers(
            @RequestAttribute("authenticatedUsername") String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {

        List<Trainer> updatedTrainers = traineeService.updateTrainingTrainers(
                new Credentials(username, null),
                request.getUpdates()
        );

        List<TraineeProfileResponse.TrainerInfo> trainerInfoList = updatedTrainers.stream()
                .map(trainer -> new TraineeProfileResponse.TrainerInfo(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization().getTrainingTypeName()
                ))
                .collect(Collectors.toList());

        logger.info("Successfully updated {} trainers for trainee: {}",
                request.getUpdates().size(), username);

        return ResponseEntity.ok(new UpdatedTrainersListResponse(trainerInfoList));
    }

    @Operation(summary = "Activate or deactivate a trainee", description = "Sets the active status for a trainee's profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainee does not exist")
    })
    @PatchMapping("/status")
    public ResponseEntity<Void> updateTraineeStatus(@Valid @RequestBody UpdateActiveStatusRequest request) {
        Trainee trainee = traineeService.getByUsername(request.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainee with username '" + request.getUsername() + "' not found."));

        if (trainee.getUser().getIsActive() != request.isActive()) {
            traineeService.updateStatus(new Credentials(request.getUsername(), request.getPassword()));
        }

        logger.info("Successfully updated status for trainee {}", request.getUsername());
        return ResponseEntity.ok().build();
    }

    private TraineeProfileResponse mapTraineeToProfileResponse(Trainee trainee) {
        TraineeProfileResponse response = new TraineeProfileResponse();
        response.setFirstName(trainee.getUser().getFirstName());
        response.setLastName(trainee.getUser().getLastName());
        response.setDateOfBirth(trainee.getDateOfBirth());
        response.setAddress(trainee.getAddress());
        response.setActive(trainee.getUser().getIsActive());

        List<TraineeProfileResponse.TrainerInfo> trainersFromTrainings = trainee.getTrainings()
                .stream()
                .map(Training::getTrainer)
                .distinct()
                .map(trainer -> new TraineeProfileResponse.TrainerInfo(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization().getTrainingTypeName()
                ))
                .collect(Collectors.toList());

        response.setTrainersList(trainersFromTrainings);
        return response;
    }
}
