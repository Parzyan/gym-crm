package com.company.gym.controller;

import com.company.gym.dao.TrainingTypeDAO;
import com.company.gym.dto.request.AddTrainingRequest;
import com.company.gym.dto.response.TraineeTrainingResponse;
import com.company.gym.dto.response.TrainerTrainingResponse;
import com.company.gym.dto.response.TrainingTypeResponse;
import com.company.gym.entity.*;
import com.company.gym.exception.InvalidCredentialsException;
import com.company.gym.service.TrainingService;
import com.company.gym.service.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingTypeDAO trainingTypeDAO;

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainingController trainingController;

    private Training testTraining;
    private TrainingType testTrainingType;
    private AddTrainingRequest addTrainingRequest;

    @BeforeEach
    void setUp() {
        User traineeUser = new User();
        traineeUser.setFirstName("John");
        traineeUser.setLastName("Doe");

        User trainerUser = new User();
        trainerUser.setFirstName("Jane");
        trainerUser.setLastName("Trainer");

        Trainee testTrainee = new Trainee();
        testTrainee.setUser(traineeUser);

        Trainer testTrainer = new Trainer();
        testTrainer.setUser(trainerUser);

        testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("CrossFit");

        testTraining = new Training();
        testTraining.setTrainingName("Morning WOD");
        testTraining.setTrainingDate(new Date());
        testTraining.setDuration(60);
        testTraining.setTrainee(testTrainee);
        testTraining.setTrainer(testTrainer);
        testTraining.setTrainingType(testTrainingType);

        addTrainingRequest = new AddTrainingRequest();
        addTrainingRequest.setTraineeUsername("john.doe");
        addTrainingRequest.setTraineePassword("pass");
        addTrainingRequest.setTrainerUsername("jane.trainer");
        addTrainingRequest.setTrainerPassword("pass");
        addTrainingRequest.setTrainingName("Morning WOD");
        addTrainingRequest.setTrainingTypeName("CrossFit");
        addTrainingRequest.setTrainingDate(new Date());
        addTrainingRequest.setTrainingDuration(60);
    }

    @Test
    @DisplayName("Get Trainee Trainings should return 200 OK with list on success")
    void getTraineeTrainings_onSuccess() {
        when(trainingService.getTraineeTrainings(any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(testTraining));

        ResponseEntity<List<TraineeTrainingResponse>> response = trainingController.getTraineeTrainings(
                "john.doe", "password", null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Morning WOD", response.getBody().getFirst().getTrainingName());
        assertEquals("Jane Trainer", response.getBody().getFirst().getTrainerName());
    }

    @Test
    @DisplayName("Get Trainee Trainings should return 401 on service exception")
    void getTraineeTrainings_onAuthFailure() {
        when(trainingService.getTraineeTrainings(any(), any(), any(), any(), any())).thenThrow(new InvalidCredentialsException("Auth failed"));

        ResponseEntity<List<TraineeTrainingResponse>> response = trainingController.getTraineeTrainings(
                "john.doe", "wrong_password", null, null, null, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Get Trainee Trainings should return 400 if Training Type is invalid")
    void getTraineeTrainings_withInvalidTrainingType() {
        when(trainingTypeDAO.findByName("NonExistentType")).thenReturn(Optional.empty());

        ResponseEntity<List<TraineeTrainingResponse>> response = trainingController.getTraineeTrainings(
                "john.doe", "password", null, null, null, "NonExistentType");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Get Trainer Trainings should return 200 OK with list on success")
    void getTrainerTrainings_onSuccess() {
        when(trainingService.getTrainerTrainings(any(), any(), any(), any())).thenReturn(Collections.singletonList(testTraining));

        ResponseEntity<List<TrainerTrainingResponse>> response = trainingController.getTrainerTrainings(
                "jane.trainer", "password", null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Morning WOD", response.getBody().getFirst().getTrainingName());
        assertEquals("John Doe", response.getBody().getFirst().getTraineeName());
    }

    @Test
    @DisplayName("Add Training should return 200 OK on success")
    void addTraining_onSuccess() {
        when(trainingTypeDAO.findByName("CrossFit")).thenReturn(Optional.of(testTrainingType));
        when(trainingService.createTraining(any(), any(), anyString(), anyLong(), any(), anyInt())).thenReturn(testTraining);

        ResponseEntity<Void> response = trainingController.addTraining(addTrainingRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainingService, times(1)).createTraining(any(), any(), anyString(), eq(1L), any(), anyInt());
    }

    @Test
    @DisplayName("Add Training should return 400 Bad Request if Training Type not found")
    void addTraining_whenTrainingTypeNotFound() {
        when(trainingTypeDAO.findByName(anyString())).thenReturn(Optional.empty());

        ResponseEntity<Void> response = trainingController.addTraining(addTrainingRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(trainingService, never()).createTraining(any(), any(), anyString(), anyLong(), any(), anyInt());
    }

    @Test
    @DisplayName("Get Training Types should return 200 OK with list")
    void getTrainingTypes_onSuccess() {
        when(trainingTypeService.getAll()).thenReturn(Collections.singletonList(testTrainingType));

        ResponseEntity<List<TrainingTypeResponse>> response = trainingController.getTrainingTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("CrossFit", response.getBody().getFirst().getName());
        assertEquals(1L, response.getBody().getFirst().getId());
    }
}
