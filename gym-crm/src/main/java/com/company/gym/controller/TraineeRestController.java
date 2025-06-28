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
import com.company.gym.service.TraineeService;
import com.company.gym.service.TrainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trainees")
public class TraineeRestController {
    private static final Logger logger = LoggerFactory.getLogger(TraineeRestController.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    @Autowired
    public TraineeRestController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

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

    @GetMapping("/profile")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(@RequestParam String username) {
        Optional<Trainee> traineeOpt = traineeService.getByUsername(username);

        if (traineeOpt.isEmpty()) {
            logger.warn("Get profile failed. Trainee not found: {}", username);
            return ResponseEntity.notFound().build();
        }

        Trainee trainee = traineeOpt.get();
        TraineeProfileResponse response = mapTraineeToProfileResponse(trainee);

        logger.info("Successfully fetched profile for trainee: {}", username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(@RequestBody UpdateTraineeProfileRequest request) {
        try {
            Credentials credentials = new Credentials(request.getUsername(), request.getPassword());

            Trainee updatedTrainee = traineeService.updateTraineeProfile(
                    credentials, request.getDateOfBirth(), request.getAddress());

            if (updatedTrainee.getUser().getIsActive() != request.isActive()) {
                traineeService.updateStatus(credentials);
            }

            Trainee finalTrainee = traineeService.getByUsername(request.getUsername()).get();
            TraineeProfileResponse response = mapTraineeToProfileResponse(finalTrainee);

            logger.info("Successfully updated profile for trainee: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch(Exception e) {
            logger.error("Update profile failed for {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteTraineeProfile(@RequestBody DeleteProfileRequest request) {
        try {
            Credentials credentials = new Credentials(request.getUsername(), request.getPassword());
            traineeService.deleteTraineeProfile(credentials);
            logger.info("Successfully deleted trainee profile: {}", request.getUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to delete trainee profile for {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/unassigned-trainers")
    public ResponseEntity<List<UnassignedTrainerResponse>> getUnassignedTrainers(@RequestParam String username) {
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
        } catch (Exception e) {
            logger.error("Failed to get unassigned trainers for trainee {}: {}", username, e.getMessage());
            return ResponseEntity.notFound().build();
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

    @PatchMapping("/status")
    public ResponseEntity<Void> updateTraineeStatus(@RequestBody UpdateActiveStatusRequest request) {
        try {
            Credentials credentials = new Credentials(request.getUsername(), request.getPassword());

            Trainee trainee = traineeService.getByUsername(request.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

            if (trainee.getUser().getIsActive() != request.isActive()) {
                traineeService.updateStatus(credentials);
            }

            logger.info("Successfully updated status for trainee {}", request.getUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to update status for trainee {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
