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
import com.company.gym.exception.InvalidCredentialsException;
import com.company.gym.service.AuthenticationService;
import com.company.gym.service.TraineeService;
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

@Tag(name = "Trainee Management", description = "Endpoints for creating, updating, and retrieving trainee data")
@RestController
@RequestMapping("/trainees")
public class TraineeRestController {
    private static final Logger logger = LoggerFactory.getLogger(TraineeRestController.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final AuthenticationService authenticationService;

    @Autowired
    public TraineeRestController(TraineeService traineeService, TrainerService trainerService, AuthenticationService authenticationService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Register a new trainee", description = "Creates a new trainee profile and returns their generated credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainee created successfully",
                    content = @Content(schema = @Schema(implementation = UserCredentialsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/register")
    public ResponseEntity<UserCredentialsResponse> registerTrainee(@RequestBody TraineeRegistrationRequest request) {
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

        } catch (IllegalArgumentException e) {
            logger.error("Registration failed: First name or last name was null.", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("An unexpected error occurred during trainee registration.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get a trainee's profile", description = "Retrieves the full profile details for a single trainee by their username. Requires credentials for authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved trainee profile",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials provided"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Authenticated user not found in database")
    })
    @GetMapping("/profile")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(
            @Parameter(description = "Username of the trainee to retrieve", required = true) @RequestParam String username,
            @Parameter(description = "Password of the trainee (for authentication)", required = true) @RequestParam String password) {

        try {
            authenticationService.authenticate(new Credentials(username, password));

            Trainee trainee = traineeService.getByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username));

            return ResponseEntity.ok(mapTraineeToProfileResponse(trainee));

        } catch (InvalidCredentialsException e) {
            logger.warn("Get profile failed for user '{}' due to invalid credentials.", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalStateException e) {
            logger.error("Data consistency error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update a trainee's profile", description = "Updates the profile information for an existing trainee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated trainee profile",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication failure"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainee does not exist")
    })
    @PutMapping("/profile")
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(@RequestBody UpdateTraineeProfileRequest request) {
        try {
            Trainee updatedTrainee = traineeService.updateTraineeProfile(
                    new Credentials(request.getUsername(), request.getPassword()),
                    request.getDateOfBirth(),
                    request.getAddress()
            );

            if (updatedTrainee.getUser().getIsActive() != request.isActive()) {
                traineeService.updateStatus(new Credentials(request.getUsername(), request.getPassword()));
            }

            Trainee finalTrainee = traineeService.getByUsername(request.getUsername()).get();
            TraineeProfileResponse response = mapTraineeToProfileResponse(finalTrainee);

            logger.info("Successfully updated profile for trainee: {}", request.getUsername());
            return ResponseEntity.ok(response);

        } catch (InvalidCredentialsException e) {
            logger.warn("Update profile failed for '{}': {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Update profile failed because trainee '{}' was not found.", request.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Delete a trainee's profile", description = "Permanently deletes a trainee's profile. Requires credentials in the body for authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee profile deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainee does not exist")
    })
    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteTraineeProfile(@RequestBody DeleteProfileRequest request) {
        try {
            traineeService.deleteTraineeProfile(new Credentials(request.getUsername(), request.getPassword()));
            logger.info("Successfully deleted trainee profile: {}", request.getUsername());
            return ResponseEntity.ok().build();
        } catch (InvalidCredentialsException e) {
            logger.warn("Delete profile failed for '{}': {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Delete profile failed because trainee '{}' was not found.", request.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Get active trainers not assigned to a trainee", description = "Retrieves a list of trainers who are active and do not have any trainings with the specified trainee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of trainers"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Not Found - Trainee does not exist")
    })
    @GetMapping("/unassigned-trainers")
    public ResponseEntity<List<UnassignedTrainerResponse>> getUnassignedTrainers(
            @Parameter(description = "Username of the trainee", required = true) @RequestParam String username,
            @Parameter(description = "Password of the trainee", required = true) @RequestParam String password) {
        try {
            authenticationService.authenticate(new Credentials(username, password));

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
        } catch (InvalidCredentialsException e) {
            logger.warn("Failed for user '{}' due to invalid credentials.", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        catch (Exception e) {
            logger.error("Failed to get unassigned trainers for trainee {}: {}", username, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update a trainee's assigned trainers list", description = "Partially updates the trainer assigned to one or more specific trainings for a trainee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated trainers list",
                    content = @Content(schema = @Schema(implementation = UpdatedTrainersListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid data or permissions issue")
    })
    @PatchMapping("/trainers")
    public ResponseEntity<UpdatedTrainersListResponse> updateTrainers(
            @RequestBody UpdateTraineeTrainersRequest request) {
        try {
            Credentials credentials = new Credentials(request.getTraineeUsername(), request.getTraineePassword());

            List<Trainer> updatedTrainersList = traineeService.updateTrainingTrainers(credentials, request.getUpdates());

            List<TraineeProfileResponse.TrainerInfo> trainerInfoList = updatedTrainersList.stream()
                    .map(trainer -> new TraineeProfileResponse.TrainerInfo(
                            trainer.getUser().getUsername(),
                            trainer.getUser().getFirstName(),
                            trainer.getUser().getLastName(),
                            trainer.getSpecialization().getTrainingTypeName()
                    )).collect(Collectors.toList());

            return ResponseEntity.ok(new UpdatedTrainersListResponse(trainerInfoList));
        } catch (Exception e) {
            logger.error("Failed to update trainee's trainers for user {}: {}", request.getTraineeUsername(), e.getMessage());
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
