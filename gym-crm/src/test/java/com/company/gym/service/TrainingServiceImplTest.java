package com.company.gym.service;

import com.company.gym.client.WorkloadServiceClient;
import com.company.gym.dao.*;
import com.company.gym.entity.*;
import com.company.gym.service.impl.AuthenticationServiceImpl;
import com.company.gym.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingDAO trainingDAO;

    @Mock
    private TraineeDAO traineeDAO;

    @Mock
    private TrainerDAO trainerDAO;

    @Mock
    private TrainingTypeDAO trainingTypeDAO;

    @Mock
    private WorkloadServiceClient workloadServiceClient;

    @Mock
    private AuthenticationServiceImpl authenticationService;

    @InjectMocks private TrainingServiceImpl trainingService;

    private Training testTraining;
    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType testTrainingType;
    private Credentials traineeCredentials;
    private Credentials trainerCredentials;

    @BeforeEach
    void setUp() {
        User traineeUser = new User();
        traineeUser.setUsername("test.trainee");
        traineeUser.setPassword("traineePass");

        User trainerUser = new User();
        trainerUser.setUsername("test.trainer");
        trainerUser.setPassword("trainerPass");
        testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("Fitness");

        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUser(traineeUser);

        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setUser(trainerUser);
        testTrainer.setSpecialization(testTrainingType);

        testTraining = new Training();
        testTraining.setId(1L);
        testTraining.setTrainee(testTrainee);
        testTraining.setTrainer(testTrainer);
        testTraining.setTrainingName("Morning Session");
        testTraining.setTrainingType(testTrainingType);
        testTraining.setTrainingDate(new Date());
        testTraining.setDuration(60);

        traineeCredentials = new Credentials("test.trainee", "traineePass");
        trainerCredentials = new Credentials("test.trainer", "trainerPass");
    }

    @Test
    void createTraining_Success() {
        when(traineeDAO.findByUsername("test.trainee")).thenReturn(Optional.of(testTrainee));
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));
        when(trainingTypeDAO.findById(1L)).thenReturn(Optional.of(testTrainingType));
        doNothing().when(trainingDAO).save(any());

        Training result = trainingService.createTraining(
                traineeCredentials, trainerCredentials, "Morning Session",
                1L, new Date(), 60);

        assertNotNull(result);
        assertEquals(testTrainee, result.getTrainee());
        verify(trainingDAO).save(any(Training.class));
    }

    @Test
    void createTraining_InvalidDuration() {
        assertThrows(IllegalArgumentException.class, () ->
                trainingService.createTraining(
                        traineeCredentials, trainerCredentials, "Morning Session",
                        1L, new Date(), 0));
    }

    @Test
    void createTraining_TraineeNotFound() {
        when(traineeDAO.findByUsername("test.trainee")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                trainingService.createTraining(
                        traineeCredentials, trainerCredentials, "Morning Session",
                        1L, new Date(), 60));

        verify(trainingDAO, never()).save(any());
    }

    @Test
    void getTraineeTrainings_Success() {
        Date fromDate = new Date();
        Date toDate = new Date();
        when(traineeDAO.findByUsername("test.trainee")).thenReturn(Optional.of(testTrainee));
        when(trainingDAO.findTrainingsByTraineeAndCriteria(1L, fromDate, toDate, "test.trainer", 1L))
                .thenReturn(List.of(testTraining));

        List<Training> result = trainingService.getTraineeTrainings(
                traineeCredentials, fromDate, toDate, "test.trainer", 1L);

        assertEquals(1, result.size());
        verify(traineeDAO).findByUsername("test.trainee");
    }

    @Test
    void getTrainerTrainings_Success() {
        Date fromDate = new Date();
        Date toDate = new Date();
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));
        when(trainingDAO.findTrainingsByTrainerAndCriteria(1L, fromDate, toDate, "test.trainee"))
                .thenReturn(List.of(testTraining));

        List<Training> result = trainingService.getTrainerTrainings(
                trainerCredentials, fromDate, toDate, "test.trainee");

        assertEquals(1, result.size());
        verify(trainerDAO).findByUsername("test.trainer");
    }

    @Test
    void getTrainingById_Success() {
        when(trainingDAO.findById(1L)).thenReturn(Optional.of(testTraining));

        Optional<Training> result = trainingService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTraining, result.get());
    }
}
