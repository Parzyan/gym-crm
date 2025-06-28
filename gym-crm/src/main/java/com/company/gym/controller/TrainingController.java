package com.company.gym.controller;

import com.company.gym.dao.TrainingTypeDAO;
import com.company.gym.dto.request.AddTrainingRequest;
import com.company.gym.dto.response.TraineeTrainingResponse;
import com.company.gym.dto.response.TrainerTrainingResponse;
import com.company.gym.dto.response.TrainingTypeResponse;
import com.company.gym.entity.Credentials;
import com.company.gym.entity.Training;
import com.company.gym.entity.TrainingType;
import com.company.gym.service.TrainingService;
import com.company.gym.service.TrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trainings")
public class TrainingController {
    private static final Logger logger = LoggerFactory.getLogger(TrainingController.class);

    private final TrainingService trainingService;
    private final TrainingTypeDAO trainingTypeDAO;
    private final TrainingTypeService trainingTypeService;

    @Autowired
    public TrainingController(TrainingService trainingService, TrainingTypeDAO trainingTypeDAO, TrainingTypeService trainingTypeService) {
        this.trainingService = trainingService;
        this.trainingTypeDAO = trainingTypeDAO;
        this.trainingTypeService = trainingTypeService;
    }

    @GetMapping("/trainee")
    public ResponseEntity<List<TraineeTrainingResponse>> getTraineeTrainings(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {

        try {
            Credentials credentials = new Credentials(username, password);

            Long trainingTypeId = null;
            if (trainingType != null && !trainingType.isEmpty()) {
                Optional<TrainingType> typeOpt = trainingTypeDAO.findByName(trainingType); // Assumes findByName exists
                if (typeOpt.isPresent()) {
                    trainingTypeId = typeOpt.get().getId();
                } else {
                    return ResponseEntity.badRequest().build();
                }
            }

            List<Training> trainings = trainingService.getTraineeTrainings(credentials, periodFrom, periodTo, trainerName, trainingTypeId);

            List<TraineeTrainingResponse> response = trainings.stream().map(t -> new TraineeTrainingResponse(
                    t.getTrainingName(),
                    t.getTrainingDate(),
                    t.getTrainingType().getTrainingTypeName(),
                    t.getDuration(),
                    t.getTrainer().getUser().getFirstName() + " " + t.getTrainer().getUser().getLastName()
            )).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get trainee trainings for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(401).build(); // Likely an auth failure
        }
    }

    @GetMapping("/trainer")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
            @RequestParam(required = false) String traineeName) {

        try {
            Credentials credentials = new Credentials(username, password);
            List<Training> trainings = trainingService.getTrainerTrainings(credentials, periodFrom, periodTo, traineeName);

            List<TrainerTrainingResponse> response = trainings.stream().map(t -> new TrainerTrainingResponse(
                    t.getTrainingName(),
                    t.getTrainingDate(),
                    t.getTrainingType().getTrainingTypeName(),
                    t.getDuration(),
                    t.getTrainee().getUser().getFirstName() + " " + t.getTrainee().getUser().getLastName()
            )).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get trainer trainings for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping
    public ResponseEntity<Void> addTraining(@RequestBody AddTrainingRequest request) {
        try {
            Credentials traineeCreds = new Credentials(request.getTraineeUsername(), request.getTraineePassword());
            Credentials trainerCreds = new Credentials(request.getTrainerUsername(), request.getTrainerPassword());

            TrainingType trainingType = trainingTypeDAO.findByName(request.getTrainingTypeName())
                    .orElseThrow(() -> new IllegalArgumentException("Training Type not found: " + request.getTrainingTypeName()));

            trainingService.createTraining(
                    traineeCreds,
                    trainerCreds,
                    request.getTrainingName(),
                    trainingType.getId(),
                    request.getTrainingDate(),
                    request.getTrainingDuration()
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to create training: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        List<TrainingType> types = trainingTypeService.getAll();
        List<TrainingTypeResponse> response = types.stream()
                .map(type -> new TrainingTypeResponse(type.getId(), type.getTrainingTypeName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
