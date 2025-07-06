package com.company.gym.controller;

import com.company.gym.dao.TrainingTypeDAO;
import com.company.gym.dto.request.AddTrainingRequest;
import com.company.gym.dto.response.TraineeTrainingResponse;
import com.company.gym.dto.response.TrainerTrainingResponse;
import com.company.gym.dto.response.TrainingTypeResponse;
import com.company.gym.entity.Credentials;
import com.company.gym.entity.Training;
import com.company.gym.entity.TrainingType;
import com.company.gym.exception.EntityNotFoundException;
import com.company.gym.service.TrainingService;
import com.company.gym.service.TrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Training Management", description = "Endpoints for creating and retrieving training records")
@RestController
@RequestMapping("/trainings")
public class TrainingController {
    private final TrainingService trainingService;
    private final TrainingTypeDAO trainingTypeDAO;
    private final TrainingTypeService trainingTypeService;

    @Autowired
    public TrainingController(TrainingService trainingService, TrainingTypeDAO trainingTypeDAO, TrainingTypeService trainingTypeService) {
        this.trainingService = trainingService;
        this.trainingTypeDAO = trainingTypeDAO;
        this.trainingTypeService = trainingTypeService;
    }

    @Operation(summary = "Get a trainee's training sessions", description = "Retrieves a list of trainings for a specific trainee, with optional filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved trainings list"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid training type provided"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Not Found - Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/trainee")
    public ResponseEntity<List<TraineeTrainingResponse>> getTraineeTrainings(
            @RequestAttribute("authenticatedUsername") String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
            @RequestParam(required = false) String trainerUsername,
            @RequestParam(required = false) String trainingType) {

        Long trainingTypeId = null;
        if (trainingType != null && !trainingType.isEmpty()) {
            TrainingType type = trainingTypeDAO.findByName(trainingType)
                    .orElseThrow(() -> new EntityNotFoundException("Training type not found: " + trainingType));
            trainingTypeId = type.getId();
        }

        List<Training> trainings = trainingService.getTraineeTrainings(
                new Credentials(username, null), periodFrom, periodTo, trainerUsername, trainingTypeId);

        List<TraineeTrainingResponse> response = trainings.stream().map(t -> new TraineeTrainingResponse(
                t.getTrainingName(),
                t.getTrainingDate(),
                t.getTrainingType().getTrainingTypeName(),
                t.getDuration(),
                t.getTrainer().getUser().getFirstName() + " " + t.getTrainer().getUser().getLastName()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get a trainer's training sessions", description = "Retrieves a list of trainings for a specific trainer, with optional filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved trainings list"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Not Found - Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/trainer")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @RequestAttribute("authenticatedUsername") String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
            @RequestParam(required = false) String traineeUsername) {

        List<Training> trainings = trainingService.getTrainerTrainings(
                new Credentials(username, null), periodFrom, periodTo, traineeUsername);

        List<TrainerTrainingResponse> response = trainings.stream().map(t -> new TrainerTrainingResponse(
                t.getTrainingName(),
                t.getTrainingDate(),
                t.getTrainingType().getTrainingTypeName(),
                t.getDuration(),
                t.getTrainee().getUser().getFirstName() + " " + t.getTrainee().getUser().getLastName()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add a new training session", description = "Creates a new training record for a trainee with a trainer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid data, or user/type not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Not Found - Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping
    public ResponseEntity<Void> addTraining(@RequestBody AddTrainingRequest request) {
        Credentials traineeCreds = new Credentials(request.getTraineeUsername(), request.getTraineePassword());
        Credentials trainerCreds = new Credentials(request.getTrainerUsername(), request.getTrainerPassword());

        TrainingType trainingType = trainingTypeDAO.findByName(request.getTrainingTypeName())
                .orElseThrow(() -> new EntityNotFoundException("Training Type not found: " + request.getTrainingTypeName()));

        trainingService.createTraining(
                traineeCreds,
                trainerCreds,
                request.getTrainingName(),
                trainingType.getId(),
                request.getTrainingDate(),
                request.getTrainingDuration()
        );
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all available training types", description = "Retrieves a list of all training types in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved training types"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        List<TrainingType> types = trainingTypeService.getAll();
        List<TrainingTypeResponse> response = types.stream()
                .map(type -> new TrainingTypeResponse(type.getId(), type.getTrainingTypeName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
