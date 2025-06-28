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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trainers")
public class TrainerRestController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerRestController.class);
    private final TrainerService trainerService;

    @Autowired
    public TrainerRestController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

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

    @GetMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(@RequestParam String username) {
        return trainerService.getByUsername(username)
                .map(this::mapTrainerToProfileResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Get profile failed. Trainer not found: {}", username);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(@RequestBody UpdateTrainerProfileRequest request) {
        try {
            Credentials credentials = new Credentials(request.getUsername(), request.getPassword());

            trainerService.getByUsername(request.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

            Trainer currentTrainer = trainerService.getByUsername(request.getUsername()).get();
            if (currentTrainer.getUser().getIsActive() != request.isActive()) {
                trainerService.updateStatus(credentials);
            }

            Trainer updatedTrainer = trainerService.getByUsername(request.getUsername()).get();
            TrainerProfileResponse response = mapTrainerToProfileResponse(updatedTrainer);

            logger.info("Successfully processed update for trainer profile: {}", request.getUsername());
            return ResponseEntity.ok(response);

        } catch (InvalidCredentialsException e) {
            logger.error("Update profile failed for {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (IllegalArgumentException e) {
            logger.error("Update profile failed for {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("An unexpected error occurred updating trainer {}", request.getUsername(), e);
            return ResponseEntity.status(500).build();
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

    @PatchMapping("/status")
    public ResponseEntity<Void> updateTrainerStatus(@RequestBody UpdateActiveStatusRequest request) {
        try {
            Credentials credentials = new Credentials(request.getUsername(), request.getPassword());

            Trainer trainer = trainerService.getByUsername(request.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

            if (trainer.getUser().getIsActive() != request.isActive()) {
                trainerService.updateStatus(credentials);
            }

            logger.info("Successfully updated status for trainer {}", request.getUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to update status for trainer {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
