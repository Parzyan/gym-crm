package com.company.gym.controller;

import com.company.gym.dao.TrainingTypeDAO;
import com.company.gym.dto.request.AddTrainingRequest;
import com.company.gym.dto.response.TraineeTrainingResponse;
import com.company.gym.dto.response.TrainerTrainingResponse;
import com.company.gym.dto.response.TrainingTypeResponse;
import com.company.gym.entity.*;
import com.company.gym.exception.EntityNotFoundException;
import com.company.gym.service.TrainingService;
import com.company.gym.service.TrainingTypeService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingTypeDAO trainingTypeDAO;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    Principal principal;

    private TrainingController trainingController;

    private Training testTraining;
    private TrainingType testTrainingType;
    private AddTrainingRequest addTrainingRequest;

    @BeforeEach
    void setUp() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("Cardio");

        Trainee testTrainee = new Trainee();
        User traineeUser = new User();
        traineeUser.setUsername("john.doe");
        traineeUser.setFirstName("John");
        traineeUser.setLastName("Doe");
        testTrainee.setUser(traineeUser);

        Trainer testTrainer = new Trainer();
        User trainerUser = new User();
        trainerUser.setUsername("jane.trainer");
        trainerUser.setFirstName("Jane");
        trainerUser.setLastName("Trainer");
        testTrainer.setUser(trainerUser);

        testTraining = new Training();
        testTraining.setId(1L);
        testTraining.setTrainingName("Morning Session");
        testTraining.setTrainingDate(LocalDate.now());
        testTraining.setTrainingType(testTrainingType);
        testTraining.setDuration(60);
        testTraining.setTrainee(testTrainee);
        testTraining.setTrainer(testTrainer);

        addTrainingRequest = new AddTrainingRequest();
        addTrainingRequest.setTrainerUsername("jane.trainer");
        addTrainingRequest.setTrainingName("Morning Session");
        addTrainingRequest.setTrainingTypeName("Cardio");
        addTrainingRequest.setTrainingDate(LocalDate.now());
        addTrainingRequest.setTrainingDuration(60);

        trainingController = new TrainingController(trainingService, trainingTypeDAO, trainingTypeService, meterRegistry);
    }

    @Test
    @DisplayName("Get Trainee Trainings should return 200 OK with filtered results")
    void getTraineeTrainings_onSuccess() {
        String username = "john.doe";
        when(principal.getName()).thenReturn(username);
        when(trainingService.getTraineeTrainings(any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(testTraining));
        when(trainingTypeDAO.findByName("Cardio")).thenReturn(Optional.of(testTrainingType));

        ResponseEntity<List<TraineeTrainingResponse>> response = trainingController.getTraineeTrainings(
                principal, null, null, null, "Cardio");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Morning Session", response.getBody().getFirst().getTrainingName());

        verify(trainingService).getTraineeTrainings(
                argThat(credentials ->
                        credentials.getUsername().equals("john.doe") &&
                                credentials.getPassword() == null
                ),
                isNull(),
                isNull(),
                isNull(),
                eq(1L)
        );
    }

    @Test
    @DisplayName("Get Trainee Trainings should throw EntityNotFoundException for invalid training type")
    void getTraineeTrainings_invalidTrainingType() {
        when(principal.getName()).thenReturn("john.doe");
        when(trainingTypeDAO.findByName("InvalidType")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainingController.getTraineeTrainings(principal, null, null, null, "InvalidType"));
    }

    @Test
    @DisplayName("Get Trainer Trainings should return 200 OK with filtered results")
    void getTrainerTrainings_onSuccess() {
        String username = "jane.trainer";
        when(principal.getName()).thenReturn(username);
        when(trainingService.getTrainerTrainings(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(testTraining));

        ResponseEntity<List<TrainerTrainingResponse>> response = trainingController.getTrainerTrainings(
                principal, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Morning Session", response.getBody().getFirst().getTrainingName());

        verify(trainingService).getTrainerTrainings(
                argThat(credentials ->
                        credentials.getUsername().equals("jane.trainer") &&
                                credentials.getPassword() == null
                ),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    @DisplayName("Add Training should return 200 OK on success")
    void addTraining_onSuccess() {
        String traineeUsername = "john.doe";
        when(principal.getName()).thenReturn(traineeUsername);

        AddTrainingRequest createRequest = new AddTrainingRequest(
                "jane.trainer",
                "Morning Session",
                "Cardio",
                LocalDate.now(),
                60
        );

        when(trainingTypeDAO.findByName("Cardio")).thenReturn(Optional.of(testTrainingType));
        when(trainingService.createTraining(any(), any(), anyString(), any(), any(LocalDate.class), anyInt()))
                .thenReturn(testTraining);

        ResponseEntity<Void> response = trainingController.addTraining(principal, createRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainingService).createTraining(
                argThat(creds ->
                        creds.getUsername().equals("john.doe")
                ),
                argThat(creds ->
                        creds.getUsername().equals("jane.trainer")
                ),
                eq("Morning Session"),
                eq(1L),
                any(LocalDate.class),
                eq(60)
        );
    }

    @Test
    @DisplayName("Get Training Types should return 200 OK with all types")
    void getTrainingTypes_onSuccess() {
        when(trainingTypeService.getAll()).thenReturn(Collections.singletonList(testTrainingType));

        ResponseEntity<List<TrainingTypeResponse>> response = trainingController.getTrainingTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Cardio", response.getBody().getFirst().getName());
    }
}
