package com.company.gym.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.dao.TrainingTypeDAO;
import com.company.gym.dto.response.UserCredentialsResponse;
import com.company.gym.entity.*;
import com.company.gym.service.impl.AuthenticationServiceImpl;
import com.company.gym.service.impl.TrainerServiceImpl;
import com.company.gym.util.PasswordGenerator;
import com.company.gym.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private TrainingTypeDAO trainingTypeDAO;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private AuthenticationServiceImpl authenticationService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer testTrainer;
    private TrainingType testTrainingType;
    private Credentials validCredentials;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test.trainer");
        testUser.setPassword("oldPassword");
        testUser.setIsActive(true);

        testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("Fitness");

        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setUser(testUser);
        testTrainer.setSpecialization(testTrainingType);

        validCredentials = new Credentials("test.trainer", "oldPassword");
    }

    @Test
    void createTrainerProfile_Success() {
        when(usernameGenerator.generateUsername("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("generatedPassword");
        when(trainingTypeDAO.findById(1L)).thenReturn(Optional.of(testTrainingType));

        UserCredentialsResponse result = trainerService.createTrainerProfile("John", "Smith", 1L);

        assertNotNull(result);
        assertEquals("John.Smith", result.getUsername());
        assertEquals("generatedPassword", result.getPassword());
        verify(trainerDAO).save(any(Trainer.class));
    }

    @Test
    void createTrainerProfile_NamesNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainerService.createTrainerProfile(null, "Smith", 1L));
        assertThrows(IllegalArgumentException.class,
                () -> trainerService.createTrainerProfile("John", null, 1L));
    }

    @Test
    void createTrainerProfile_InvalidSpecialization() {
        when(trainingTypeDAO.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> trainerService.createTrainerProfile("John", "Smith", 1L));
    }

    @Test
    void changePassword_Success() {
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        trainerService.changePassword("test.trainer", "oldPassword", "newPassword");

        assertEquals("encodedNewPassword", testTrainer.getUser().getPassword());
        verify(trainerDAO).update(testTrainer);
    }

    @Test
    void changePassword_TrainerNotFound() {
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> trainerService.changePassword("test.trainer", "oldPassword", "newPassword"));
    }

    void updateTrainerProfile_Success() {
        TrainingType newTrainingType = new TrainingType();
        newTrainingType.setId(2L);
        newTrainingType.setTrainingTypeName("Yoga");

        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));
        when(trainingTypeDAO.findById(2L)).thenReturn(Optional.of(newTrainingType));

        Trainer result = trainerService.updateTrainerProfile(validCredentials, 2L);

        assertEquals(newTrainingType, result.getSpecialization());
        verify(trainerDAO).update(testTrainer);
    }

    @Test
    void updateTrainerProfile_TrainingTypeNotFound() {
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));
        when(trainingTypeDAO.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> trainerService.updateTrainerProfile(validCredentials, 2L));

        verify(trainerDAO, never()).update(any());
    }

    @Test
    void updateStatus_Success() {
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));

        trainerService.updateStatus(validCredentials);
        assertFalse(testTrainer.getUser().getIsActive());

        trainerService.updateStatus(validCredentials);
        assertTrue(testTrainer.getUser().getIsActive());

        verify(trainerDAO, times(2)).update(testTrainer);
    }

    @Test
    void getTrainersBySpecialization_Success() {
        List<Trainer> expectedTrainers = Collections.singletonList(testTrainer);
        when(trainerDAO.findBySpecialization(1L)).thenReturn(expectedTrainers);

        List<Trainer> result = trainerService.getTrainersBySpecialization(1L);

        assertEquals(1, result.size());
        assertEquals(testTrainer, result.getFirst());
    }

    @Test
    void getUnassignedTrainers_Success() {
        Trainee testTrainee = new Trainee();
        testTrainee.setId(1L);
        List<Trainer> expectedTrainers = Collections.singletonList(testTrainer);

        when(traineeDAO.findByUsername("test.trainee")).thenReturn(Optional.of(testTrainee));
        when(trainerDAO.findTrainersNotAssignedToTrainee(1L)).thenReturn(expectedTrainers);

        List<Trainer> result = trainerService.getUnassignedTrainers("test.trainee");

        assertEquals(1, result.size());
        assertEquals(testTrainer, result.getFirst());
    }

    @Test
    void getUnassignedTrainers_TraineeNotFound() {
        when(traineeDAO.findByUsername("test.trainee")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> trainerService.getUnassignedTrainers("test.trainee"));
    }

    @Test
    void getTrainerProfileById_Success() {
        when(trainerDAO.findById(1L)).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> result = trainerService.getById(1L);
        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }
}
