package com.company.gym.service;

import com.company.gym.dao.*;
import com.company.gym.dao.impl.TrainingDAOImpl;
import com.company.gym.entity.*;
import com.company.gym.service.impl.AuthenticationServiceImpl;
import com.company.gym.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainingServiceImplTest {

    private TrainingDAO trainingDAO;
    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;
    private TrainingTypeDAO trainingTypeDAO;
    private AuthenticationServiceImpl authenticationService;

    private TrainingServiceImpl trainingService;

    private Training testTraining;
    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType testTrainingType;

    @BeforeEach
    void setUp() {
        trainingDAO = mock(TrainingDAO.class);
        traineeDAO = mock(TraineeDAO.class);
        trainerDAO = mock(TrainerDAO.class);
        trainingTypeDAO = mock(TrainingTypeDAO.class);
        authenticationService = mock(AuthenticationServiceImpl.class);

        trainingService = new TrainingServiceImpl();
        trainingService.setTrainingDAO(trainingDAO);
        trainingService.setTraineeDAO(traineeDAO);
        trainingService.setTrainerDAO(trainerDAO);
        trainingService.setTrainingTypeDAO(trainingTypeDAO);
        trainingService.setAuthenticationService(authenticationService);

        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUser(new User());
        testTrainee.getUser().setUsername("trainee1");

        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setUser(new User());
        testTrainer.getUser().setUsername("trainer1");

        testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("Yoga");

        testTraining = new Training();
        testTraining.setId(1L);
        testTraining.setTrainee(testTrainee);
        testTraining.setTrainer(testTrainer);
        testTraining.setTrainingType(testTrainingType);
        testTraining.setTrainingName("Morning Session");
        testTraining.setTrainingDate(new Date());
        testTraining.setDuration(60);

        when(authenticationService.authenticateUser(any(), any())).thenReturn(true);
    }

    @Test
    void createTraining_Success() {
        when(traineeDAO.findByUsername("trainee1")).thenReturn(Optional.of(testTrainee));
        when(trainerDAO.findByUsername("trainer1")).thenReturn(Optional.of(testTrainer));
        when(trainingTypeDAO.findById(1L)).thenReturn(Optional.of(testTrainingType));

        Training result = trainingService.createTraining("trainee1", "password12",
                "trainer1", "password12",
                "Morning Session", 1L, new Date(), 60);

        assertNotNull(result);
        assertEquals("Morning Session", result.getTrainingName());
        verify(trainingDAO, times(1)).save(any(Training.class));
    }

    @Test
    void getTrainingById_Success() {
        when(trainingDAO.findById(1L)).thenReturn(Optional.of(testTraining));
        Optional<Training> result = trainingService.getById(1L);
        assertTrue(result.isPresent());
        assertEquals(testTraining, result.get());
    }

    @Test
    void getTrainingsByTraineeAndCriteria() {
        when(traineeDAO.findByUsername("trainee1")).thenReturn(Optional.of(testTrainee));
        when(trainingDAO.findTrainingsByTraineeAndCriteria(1L, null, null, null, null))
                .thenReturn(Collections.singletonList(testTraining));

        List<Training> result = trainingService.getTraineeTrainings( "password12",
                "trainee1", null, null, null, null);

        assertEquals(1, result.size());
        assertEquals(testTraining, result.get(0));
    }

    @Test
    void getTrainingsByTrainerAndCriteria() {
        when(trainerDAO.findByUsername("trainer1")).thenReturn(Optional.of(testTrainer));
        when(trainingDAO.findTrainingsByTrainerAndCriteria(1L, null, null, null))
                .thenReturn(Collections.singletonList(testTraining));

        List<Training> result = trainingService.getTrainerTrainings( "password12",
                "trainer1", null, null, null);

        assertEquals(1, result.size());
        assertEquals(testTraining, result.get(0));
    }

    @Test
    void createTraining_InvalidDuration() {
        assertThrows(IllegalArgumentException.class, () ->
                trainingService.createTraining("trainee1", "password12",
                        "trainer1", "password12",
                        "Session", 1L, new Date(), -1));
    }

    @Test
    void createTraining_TraineeNotFound() {
        when(traineeDAO.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                trainingService.createTraining("unknown", "password12",
                        "trainer1", "password12",
                        "Session", 1L, new Date(), 60));
    }
}