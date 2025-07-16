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

    @Mock
    private Principal principal;

    private TraineeController traineeController;

    private Trainee testTrainee;
    private TraineeRegistrationRequest registrationRequest;
    private UpdateTraineeProfileRequest updateRequest;
    private UpdateTraineeTrainersRequest updateTrainerRequest;
    private UpdateActiveStatusRequest statusRequest;
    private final String TEST_USERNAME = "john.doe";

    @BeforeEach
    void setUp() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

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
        List<UpdateTraineeTrainersRequest.TrainingTrainerUpdate> updates = getTrainingTrainerUpdates();
        updateTrainerRequest.setUpdates(updates);

        statusRequest = new UpdateActiveStatusRequest();
        statusRequest.setUsername("john.doe");
        statusRequest.setPassword("password123");
        statusRequest.setActive(false);

        traineeController = new TraineeController(traineeService, trainerService, meterRegistry);
    }

    private static List<UpdateTraineeTrainersRequest.TrainingTrainerUpdate> getTrainingTrainerUpdates() {
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
        return updates;
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
        UserCredentialsResponse credentials = new UserCredentialsResponse(TEST_USERNAME, "password");
        when(traineeService.createTraineeProfile(anyString(), anyString(), any(), anyString())).thenReturn(credentials);

        ResponseEntity<UserCredentialsResponse> response = traineeController.registerTrainee(registrationRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_USERNAME, response.getBody().getUsername());
    }

    @Test
    @DisplayName("Get Trainee Profile should return 200 OK")
    void getTraineeProfile_onSuccess() {
        when(principal.getName()).thenReturn(TEST_USERNAME);
        when(traineeService.getByUsername(TEST_USERNAME)).thenReturn(Optional.of(testTrainee));

        ResponseEntity<TraineeProfileResponse> response = traineeController.getTraineeProfile(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());
    }

    @Test
    @DisplayName("Get Trainee Profile should throw EntityNotFoundException if trainee does not exist")
    void getTraineeProfile_traineeNotFound() {
        when(principal.getName()).thenReturn(TEST_USERNAME);
        when(traineeService.getByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> traineeController.getTraineeProfile(principal));
    }

    @Test
    @DisplayName("Update Trainee Profile should return 200 OK on success")
    void updateTraineeProfile_onSuccess() {
        when(principal.getName()).thenReturn(TEST_USERNAME);
        testTrainee.getUser().setIsActive(false);
        when(traineeService.updateTraineeProfile(any(), any(), any())).thenReturn(testTrainee);
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        ResponseEntity<TraineeProfileResponse> response = traineeController.updateTraineeProfile(principal, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());

        verify(traineeService, times(1)).updateStatus(argThat(credentials ->
                credentials.getUsername().equals("john.doe") && credentials.getPassword() == null
        ));

        verify(traineeService, times(1)).updateTraineeProfile(
                argThat(credentials -> credentials.getUsername().equals(TEST_USERNAME) && credentials.getPassword() == null),
                eq(updateRequest.getDateOfBirth()),
                eq(updateRequest.getAddress())
        );
    }

    @Test
    @DisplayName("Update training trainers should return 200 OK on success")
    void updateTrainingTrainers_onSuccess() {
        when(principal.getName()).thenReturn(TEST_USERNAME);
        List<Trainer> updatedTrainers = List.of(
                createTrainer("trainer.jane", "Jane", "Doe", "Yoga"),
                createTrainer("trainer.mark", "Mark", "Smith", "Pilates")
        );

        when(traineeService.updateTrainingTrainers(any(), any())).thenReturn(updatedTrainers);

        ResponseEntity<UpdatedTrainersListResponse> response = traineeController
                .updateTrainingTrainers(principal, updateTrainerRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTrainers().size());

        verify(traineeService, times(1)).updateTrainingTrainers(
                argThat(credentials -> credentials.getUsername().equals(TEST_USERNAME) && credentials.getPassword() == null),
                eq(updateTrainerRequest.getUpdates())
        );
    }

    @Test
    @DisplayName("Update should throw EntityNotFoundException if training not found")
    void updateTrainingTrainers_trainingNotFound() {
        when(principal.getName()).thenReturn(TEST_USERNAME);
        when(traineeService.updateTrainingTrainers(any(Credentials.class), anyList()))
                .thenThrow(new EntityNotFoundException("Training not found"));

        assertThrows(EntityNotFoundException.class, () -> traineeController.updateTrainingTrainers(principal, updateTrainerRequest));
    }

    @Test
    @DisplayName("Get Unassigned Trainers should return 200 OK with filtered list")
    void getUnassignedTrainers_onSuccess() {
        when(principal.getName()).thenReturn(TEST_USERNAME);
        Trainer activeTrainer = new Trainer();
        User activeUser = new User();
        activeUser.setIsActive(true);
        activeTrainer.setUser(activeUser);
        activeTrainer.setSpecialization(new TrainingType());

        when(trainerService.getUnassignedTrainers("john.doe")).thenReturn(Collections.singletonList(activeTrainer));

        ResponseEntity<List<UnassignedTrainerResponse>> response = traineeController.getUnassignedTrainers(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Activate Trainee should return 200 OK on success")
    void activateTrainee_onSuccess() {
        when(traineeService.getByUsername(TEST_USERNAME)).thenReturn(Optional.of(testTrainee));
        doNothing().when(traineeService).updateStatus(any(Credentials.class));

        ResponseEntity<Void> response = traineeController.updateTraineeStatus(statusRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).updateStatus(any(Credentials.class));
    }

    @Test
    @DisplayName("Delete Trainee Profile should return 204 No Content")
    void deleteTraineeProfile_onSuccess() {
        when(principal.getName()).thenReturn(TEST_USERNAME);
        doNothing().when(traineeService).deleteTraineeProfile(any(Credentials.class));

        ResponseEntity<Void> response = traineeController.deleteTraineeProfile(principal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(traineeService, times(1)).deleteTraineeProfile(any(Credentials.class));
    }
}
