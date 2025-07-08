package com.company.gym.controller;

import com.company.gym.dto.request.*;
import com.company.gym.dto.response.TraineeProfileResponse;
import com.company.gym.dto.response.UnassignedTrainerResponse;
import com.company.gym.dto.response.UpdatedTrainersListResponse;
import com.company.gym.dto.response.UserCredentialsResponse;
import com.company.gym.entity.*;
import com.company.gym.exception.EntityNotFoundException;
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
class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TraineeController traineeController;

    private Trainee testTrainee;
    private TraineeRegistrationRequest registrationRequest;
    private UpdateTraineeProfileRequest updateRequest;
    private UpdateTraineeTrainersRequest updateTrainerRequest;
    private UpdateActiveStatusRequest statusRequest;

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
        testTrainee.setTrainings(Collections.singletonList(getTraining()));

        registrationRequest = new TraineeRegistrationRequest();
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");
        registrationRequest.setDateOfBirth(new Date());
        registrationRequest.setAddress("123 Main St");

        updateRequest = new UpdateTraineeProfileRequest();
        updateRequest.setDateOfBirth(new Date());
        updateRequest.setAddress("456 Elm St");
        updateRequest.setActive(true);

        updateTrainerRequest = new UpdateTraineeTrainersRequest();
        List<UpdateTraineeTrainersRequest.TrainingTrainerUpdate> updates = new ArrayList<>();

        UpdateTraineeTrainersRequest.TrainingTrainerUpdate update1 =
                new UpdateTraineeTrainersRequest.TrainingTrainerUpdate();
        update1.setTrainingId(1L);
        update1.setTrainerUsername("trainer.jane");

        UpdateTraineeTrainersRequest.TrainingTrainerUpdate update2 =
                new UpdateTraineeTrainersRequest.TrainingTrainerUpdate();
        update2.setTrainingId(2L);
        update2.setTrainerUsername("trainer.mark");

        updates.add(update1);
        updates.add(update2);
        updateTrainerRequest.setUpdates(updates);

        statusRequest = new UpdateActiveStatusRequest();
        statusRequest.setUsername("john.doe");
        statusRequest.setPassword("password123");
        statusRequest.setActive(false);
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
        testTraining.setId(1L);
        testTraining.setTrainee(testTrainee);
        testTraining.setTrainer(testTrainer);
        return testTraining;
    }

    private Trainer createTrainer(String username, String firstName, String lastName, String specialization) {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        trainer.setUser(user);

        TrainingType type = new TrainingType();
        type.setTrainingTypeName(specialization);
        trainer.setSpecialization(type);

        return trainer;
    }

    @Test
    @DisplayName("Register Trainee should return 201 Created on success")
    void registerTrainee_onSuccess() {
        when(traineeService.createTraineeProfile(anyString(), anyString(), any(), anyString())).thenReturn(testTrainee);

        ResponseEntity<UserCredentialsResponse> response = traineeController.registerTrainee(registrationRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe", response.getBody().getUsername());
    }

    @Test
    @DisplayName("Get Trainee Profile should return 200 OK")
    void getTraineeProfile_onSuccess() {
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        ResponseEntity<TraineeProfileResponse> response = traineeController.getTraineeProfile("john.doe");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());
    }

    @Test
    @DisplayName("Get Trainee Profile should throw EntityNotFoundException if trainee does not exist")
    void getTraineeProfile_traineeNotFound() {
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> traineeController.getTraineeProfile("john.doe"));
    }

    @Test
    @DisplayName("Update Trainee Profile should return 200 OK on success")
    void updateTraineeProfile_onSuccess() {
        testTrainee.getUser().setIsActive(false);
        when(traineeService.updateTraineeProfile(any(), any(), any())).thenReturn(testTrainee);
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        ResponseEntity<TraineeProfileResponse> response = traineeController.updateTraineeProfile("john.doe", updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());

        verify(traineeService, times(1)).updateStatus(argThat(credentials ->
                credentials.getUsername().equals("john.doe") && credentials.getPassword() == null
        ));

        verify(traineeService, times(1)).updateTraineeProfile(
                argThat(credentials -> credentials.getUsername().equals("john.doe") && credentials.getPassword() == null),
                eq(updateRequest.getDateOfBirth()),
                eq(updateRequest.getAddress())
        );
    }

    @Test
    @DisplayName("Update training trainers should return 200 OK on success")
    void updateTrainingTrainers_onSuccess() {
        List<Trainer> updatedTrainers = List.of(
                createTrainer("trainer.jane", "Jane", "Doe", "Yoga"),
                createTrainer("trainer.mark", "Mark", "Smith", "Pilates")
        );

        when(traineeService.updateTrainingTrainers(any(), any())).thenReturn(updatedTrainers);

        ResponseEntity<UpdatedTrainersListResponse> response = traineeController
                .updateTrainingTrainers("john.doe", updateTrainerRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTrainers().size());

        verify(traineeService, times(1)).updateTrainingTrainers(
                argThat(credentials -> credentials.getUsername().equals("john.doe") && credentials.getPassword() == null),
                eq(updateTrainerRequest.getUpdates())
        );
    }

    @Test
    @DisplayName("Update should throw EntityNotFoundException if training not found")
    void updateTrainingTrainers_trainingNotFound() {
        when(traineeService.updateTrainingTrainers(any(Credentials.class), anyList()))
                .thenThrow(new EntityNotFoundException("Training not found"));

        assertThrows(EntityNotFoundException.class, () -> traineeController.updateTrainingTrainers("john.doe", updateTrainerRequest));
    }

    @Test
    @DisplayName("Get Unassigned Trainers should return 200 OK with filtered list")
    void getUnassignedTrainers_onSuccess() {
        Trainer activeTrainer = new Trainer();
        User activeUser = new User();
        activeUser.setIsActive(true);
        activeTrainer.setUser(activeUser);
        activeTrainer.setSpecialization(new TrainingType());

        when(trainerService.getUnassignedTrainers("john.doe")).thenReturn(Collections.singletonList(activeTrainer));

        ResponseEntity<List<UnassignedTrainerResponse>> response = traineeController.getUnassignedTrainers("john.doe");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Activate Trainee should return 200 OK on success")
    void activateTrainee_onSuccess() {
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        doNothing().when(traineeService).updateStatus(any(Credentials.class));

        ResponseEntity<Void> response = traineeController.updateTraineeStatus(statusRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).updateStatus(any(Credentials.class));
    }

    @Test
    @DisplayName("Delete Trainee Profile should return 204 No Content")
    void deleteTraineeProfile_onSuccess() {
        doNothing().when(traineeService).deleteTraineeProfile(any(Credentials.class));

        ResponseEntity<Void> response = traineeController.deleteTraineeProfile("john.doe");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(traineeService, times(1)).deleteTraineeProfile(any(Credentials.class));
    }
}
