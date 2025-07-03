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
import com.company.gym.exception.InvalidCredentialsException;
import com.company.gym.exception.InvalidInputException;
import com.company.gym.exception.ServiceException;
import com.company.gym.service.TraineeService;
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

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Trainee Management", description = "Endpoints for creating, updating, and retrieving trainee data")
@RestController
@RequestMapping("/trainees")
public class TraineeController {
    private static final Logger logger = LoggerFactory.getLogger(TraineeController.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    @Autowired
    public TraineeController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
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
        try {
            Trainee newTrainee = traineeService.createTraineeProfile(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getDateOfBirth(),
                    request.getAddress()
            );

            UserCredentialsResponse response = new UserCredentialsResponse(
                    newTrainee.getUser().getUsername(),
                    newTrainee.getUser().getPassword()
            );

            logger.info("Successfully registered trainee. Username: {}", response.getUsername());
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (InvalidInputException e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ServiceException e) {
            logger.error("Service error during trainee registration: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
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
        try {
            Trainee trainee = traineeService.getByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Trainee", username));
            return ResponseEntity.ok(mapTraineeToProfileResponse(trainee));
        } catch (EntityNotFoundException e) {
            logger.warn("Trainee not found: {}", username);
            return ResponseEntity.notFound().build();
        } catch (ServiceException e) {
            logger.error("Service error while fetching trainee profile: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
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
        try {
            Trainee updatedTrainee = traineeService.updateTraineeProfile(
                    new Credentials(username, null),
                    request.getDateOfBirth(),
                    request.getAddress()
            );

            if (updatedTrainee.getUser().getIsActive() != request.isActive()) {
                traineeService.updateStatus(new Credentials(username, null));
            }

            Trainee finalTrainee = traineeService.getByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Trainee", username));

            TraineeProfileResponse response = mapTraineeToProfileResponse(finalTrainee);

            logger.info("Successfully updated profile for trainee: {}", username);
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            logger.warn("Update profile failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (InvalidInputException e) {
            logger.warn("Invalid input for update profile: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete a trainee's profile", description = "Permanently deletes a trainee's profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Trainee profile deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainee does not exist")
    })
    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteTraineeProfile(
            @RequestAttribute("authenticatedUsername") String username) {
        try {
            traineeService.deleteTraineeProfile(new Credentials(username, null));
            logger.info("Successfully deleted trainee profile: {}", username);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.warn("Delete profile failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get active trainers not assigned to a trainee", description = "Retrieves a list of trainers who are active and do not have any trainings with the specified trainee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of trainers"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainee does not exist")
    })
    @GetMapping("/unassigned-trainers")
    public ResponseEntity<List<UnassignedTrainerResponse>> getUnassignedTrainers(
            @RequestAttribute("authenticatedUsername") String username) {
        try {
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
        } catch (EntityNotFoundException e) {
            logger.warn("Trainee not found: {}", username);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update a trainee's assigned trainer for a specific training", description = "Updates the trainer assigned to a specific training for a trainee and returns the updated list of all trainers who have trainings with that trainee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated trainer for the training",
                    content = @Content(schema = @Schema(implementation = UpdatedTrainersListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid data or permissions issue"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainee, training, or trainer not found")
    })
    @PatchMapping("/trainers")
    public ResponseEntity<UpdatedTrainersListResponse> updateTrainerForTraining(
            @RequestAttribute("authenticatedUsername") String username,
            @Valid @RequestBody UpdateTrainerForTrainingRequest request) {
        try {
            traineeService.updateTrainerForTraining(
                    new Credentials(username, null),
                    request.getTrainingId(),
                    request.getNewTrainerId()
            );

            List<Trainer> updatedTrainersList = traineeService.getTrainersForTrainee(username);

            List<TraineeProfileResponse.TrainerInfo> trainerInfoList = updatedTrainersList.stream()
                    .map(trainer -> new TraineeProfileResponse.TrainerInfo(
                            trainer.getUser().getUsername(),
                            trainer.getUser().getFirstName(),
                            trainer.getUser().getLastName(),
                            trainer.getSpecialization().getTrainingTypeName()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new UpdatedTrainersListResponse(trainerInfoList));

        } catch (EntityNotFoundException e) {
            logger.warn("Entity not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (InvalidInputException e) {
            logger.warn("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Activate or deactivate a trainee", description = "Sets the active status for a trainee's profile. Requires credentials in the body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainee does not exist")
    })
    @PatchMapping("/status")
    public ResponseEntity<Void> updateTraineeStatus(@RequestBody UpdateActiveStatusRequest request) {
        try {
            Trainee trainee = traineeService.getByUsername(request.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

            if (trainee.getUser().getIsActive() != request.isActive()) {
                traineeService.updateStatus(new Credentials(request.getUsername(), request.getPassword()));
            }

            logger.info("Successfully updated status for trainee {}", request.getUsername());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Status update failed because trainee '{}' was not found.", request.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidCredentialsException e) {
            logger.warn("Authentication failed during status update for trainee '{}': {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
