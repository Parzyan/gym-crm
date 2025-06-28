package com.company.gym.controller;

import com.company.gym.dto.request.*;
import com.company.gym.dto.response.TraineeProfileResponse;
import com.company.gym.dto.response.UnassignedTrainerResponse;
import com.company.gym.dto.response.UpdatedTrainersListResponse;
import com.company.gym.dto.response.UserCredentialsResponse;
import com.company.gym.entity.*;
import com.company.gym.service.TraineeService;
import com.company.gym.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeRestControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TraineeRestController traineeRestController;

    private Trainee testTrainee;
    private TraineeRegistrationRequest registrationRequest;
    private UpdateTraineeProfileRequest updateRequest;
    private DeleteProfileRequest deleteRequest;
    private UpdateActiveStatusRequest statusRequest;
    private UpdateTraineeTrainersRequest patchTrainersRequest;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("john.doe");
        testUser.setPassword("password123");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setIsActive(true);

        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUser(testUser);
        testTrainee.setAddress("123 Main St");
        testTrainee.setDateOfBirth(new Date());

        Training testTraining = getTraining();

        testTrainee.setTrainings(Collections.singletonList(testTraining));

        registrationRequest = new TraineeRegistrationRequest();
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");

        updateRequest = new UpdateTraineeProfileRequest();
        updateRequest.setUsername("john.doe");
        updateRequest.setPassword("password123");
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Doe");
        updateRequest.setActive(true);

        deleteRequest = new DeleteProfileRequest();
        deleteRequest.setUsername("john.doe");
        deleteRequest.setPassword("password123");

        statusRequest = new UpdateActiveStatusRequest();
        statusRequest.setUsername("john.doe");
        statusRequest.setPassword("password123");

        patchTrainersRequest = new UpdateTraineeTrainersRequest();
        patchTrainersRequest.setTraineeUsername("john.doe");
        patchTrainersRequest.setTraineePassword("password123");
    }

    private Training getTraining() {
        TrainingType testTrainingType = new TrainingType();
        testTrainingType.setTrainingTypeName("Cardio");

        Trainer testTrainer = new Trainer();
        User trainerUser = new User();
        trainerUser.setUsername("jane.trainer");
        trainerUser.setFirstName("Jane");
        trainerUser.setLastName("Trainer");
        testTrainer.setUser(trainerUser);
        testTrainer.setSpecialization(testTrainingType);

        Training testTraining = new Training();
        testTraining.setTrainee(testTrainee);
        testTraining.setTrainer(testTrainer);
        return testTraining;
    }

    @Test
    @DisplayName("Register Trainee should return 201 Created on success")
    void registerTrainee_onSuccess() {
        when(traineeService.createTraineeProfile(any(), any(), any(), any())).thenReturn(testTrainee);

        ResponseEntity<UserCredentialsResponse> response = traineeRestController.registerTrainee(registrationRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe", response.getBody().getUsername());
    }

    @Test
    @DisplayName("Register Trainee should return 400 Bad Request on failure")
    void registerTrainee_onFailure() {
        when(traineeService.createTraineeProfile(any(), any(), any(), any())).thenThrow(new IllegalArgumentException());

        ResponseEntity<UserCredentialsResponse> response = traineeRestController.registerTrainee(registrationRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Get Trainee Profile should return 200 OK with data when found")
    void getTraineeProfile_whenFound() {
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        ResponseEntity<TraineeProfileResponse> response = traineeRestController.getTraineeProfile("john.doe");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals(1, response.getBody().getTrainersList().size());
        assertEquals("jane.trainer", response.getBody().getTrainersList().getFirst().getUsername());
    }

    @Test
    @DisplayName("Get Trainee Profile should return 404 Not Found when not found")
    void getTraineeProfile_whenNotFound() {
        when(traineeService.getByUsername("unknown.user")).thenReturn(Optional.empty());

        ResponseEntity<TraineeProfileResponse> response = traineeRestController.getTraineeProfile("unknown.user");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Update Trainee Profile should return 200 OK on success")
    void updateTraineeProfile_onSuccess() {
        when(traineeService.updateTraineeProfile(any(Credentials.class), any(), any())).thenReturn(testTrainee);
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        ResponseEntity<TraineeProfileResponse> response = traineeRestController.updateTraineeProfile(updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());
        verify(traineeService, never()).updateStatus(any());
    }

    @Test
    @DisplayName("Update Trainee Profile should call updateStatus when isActive changes")
    void updateTraineeProfile_whenStatusChanges() {
        updateRequest.setActive(false);
        when(traineeService.updateTraineeProfile(any(Credentials.class), any(), any())).thenReturn(testTrainee);
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        traineeRestController.updateTraineeProfile(updateRequest);

        verify(traineeService, times(1)).updateStatus(any(Credentials.class));
    }

    @Test
    @DisplayName("Delete Trainee Profile should return 200 OK on success")
    void deleteTraineeProfile_onSuccess() {
        doNothing().when(traineeService).deleteTraineeProfile(any(Credentials.class));

        ResponseEntity<Void> response = traineeRestController.deleteTraineeProfile(deleteRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).deleteTraineeProfile(any(Credentials.class));
    }

    @Test
    @DisplayName("Get Unassigned Trainers should return 200 OK with filtered list")
    void getUnassignedTrainers_onSuccess() {
        Trainer activeUnassignedTrainer = new Trainer();
        User activeUser = new User();
        activeUser.setIsActive(true);
        activeUnassignedTrainer.setUser(activeUser);
        activeUnassignedTrainer.setSpecialization(new TrainingType());

        Trainer inactiveUnassignedTrainer = new Trainer();
        User inactiveUser = new User();
        inactiveUser.setIsActive(false);
        inactiveUnassignedTrainer.setUser(inactiveUser);

        when(trainerService.getUnassignedTrainers(anyString())).thenReturn(Arrays.asList(activeUnassignedTrainer, inactiveUnassignedTrainer));

        ResponseEntity<List<UnassignedTrainerResponse>> response = traineeRestController.getUnassignedTrainers("john.doe");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Update Trainers should return 200 OK on success")
    void updateTrainers_onSuccess() {
        when(traineeService.updateTrainingTrainers(any(), any())).thenReturn(Collections.emptyList());

        ResponseEntity<UpdatedTrainersListResponse> response = traineeRestController.updateTrainers(patchTrainersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).updateTrainingTrainers(any(), any());
    }

    @Test
    @DisplayName("Update Status should return 200 OK on success")
    void updateTraineeStatus_whenStatusChanges() {
        statusRequest.setActive(false);
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        doNothing().when(traineeService).updateStatus(any(Credentials.class));

        ResponseEntity<Void> response = traineeRestController.updateTraineeStatus(statusRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).updateStatus(any(Credentials.class));
    }
}
