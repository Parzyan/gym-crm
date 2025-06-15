package com.company.gym.service;

import com.company.gym.dao.*;
import com.company.gym.dao.impl.TraineeDAOImpl;
import com.company.gym.dao.impl.TrainerDAOImpl;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.TrainingType;
import com.company.gym.entity.User;
import com.company.gym.service.impl.AuthenticationServiceImpl;
import com.company.gym.service.impl.TrainerServiceImpl;
import com.company.gym.util.PasswordGenerator;
import com.company.gym.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceImplTest {

    private TrainerDAO trainerDAO;
    private TrainingDAO trainingDAO;
    private TrainingTypeDAO trainingTypeDAO;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private AuthenticationServiceImpl authenticationService;

    private TrainerServiceImpl trainerService;

    private Trainer testTrainer;
    private User testUser;
    private TrainingType testTrainingType;

    @BeforeEach
    void setUp() {
        trainerDAO = mock(TrainerDAO.class);
        trainingTypeDAO = mock(TrainingTypeDAO.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
        authenticationService = mock(AuthenticationServiceImpl.class);

        trainerService = new TrainerServiceImpl();
        trainerService.setTrainerDAO(trainerDAO);
        trainerService.setTrainingTypeDAO(trainingTypeDAO);
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);
        trainerService.setAuthenticationService(authenticationService);

        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Jane");
        testUser.setLastName("Smith");
        testUser.setUsername("Jane.Smith");
        testUser.setPassword("password12");
        testUser.setIsActive(true);

        testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("Yoga");

        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setUser(testUser);
        testTrainer.setSpecialization(testTrainingType);

        when(authenticationService.authenticateUser(any(), any())).thenReturn(true);
    }

    @Test
    void createTrainerProfile_Success() {
        when(usernameGenerator.generateUsername("Jane", "Smith")).thenReturn("Jane.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("password12");
        when(trainingTypeDAO.findById(any(Long.class))).thenReturn(Optional.of(testTrainingType));

        Trainer result = trainerService.createTrainerProfile("Jane", "Smith", 1L);

        assertNotNull(result);
        assertEquals("Jane.Smith", result.getUser().getUsername());
        assertEquals("password12", result.getUser().getPassword());
        assertTrue(result.getUser().getIsActive());
        verify(trainerDAO, times(1)).save(any(Trainer.class));
    }

    @Test
    void getTrainerByUsername_Success() {
        when(trainerDAO.findByUsername("Jane.Smith")).thenReturn(Optional.of(testTrainer));
        Optional<Trainer> result = trainerService.getByUsername("Jane.Smith");
        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    void getTrainerByUsername_NotFound() {
        when(trainerDAO.findByUsername("unknown")).thenReturn(Optional.empty());
        Optional<Trainer> result = trainerService.getByUsername("unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    void getTrainerById_Success() {
        when(trainerDAO.findById(1L)).thenReturn(Optional.of(testTrainer));
        Optional<Trainer> result = trainerService.getById(1L);
        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    void getTrainerById_NotFound() {
        when(trainerDAO.findById(100L)).thenReturn(Optional.empty());
        Optional<Trainer> result = trainerService.getById(100L);
        assertTrue(result.isEmpty());
    }

    @Test
    void changeTrainerPassword_Success() {
        when(trainerDAO.findByUsername("Jane.Smith")).thenReturn(Optional.of(testTrainer));

        trainerService.changeTrainerPassword("Jane.Smith", "password12", "password13");

        assertEquals("password13", testTrainer.getUser().getPassword());
        verify(trainerDAO, times(1)).update(testTrainer);
    }

    @Test
    void changeTrainerPassword_Failure_WrongOldPassword() {
        when(trainerDAO.findByUsername("Jane.Smith")).thenReturn(Optional.of(testTrainer));

        assertThrows(SecurityException.class, () ->
                trainerService.changeTrainerPassword("Jane.Smith", "password", "password13"));
    }

    @Test
    void updateTrainerStatus() {
        when(trainerDAO.findByUsername("Jane.Smith")).thenReturn(Optional.of(testTrainer));

        trainerService.updateTrainerStatus("password12", "Jane.Smith");

        assertFalse(testTrainer.getUser().getIsActive());
        verify(trainerDAO, times(1)).update(testTrainer);
    }

    @Test
    void createTrainerProfile_MissingFirstName() {
        assertThrows(IllegalArgumentException.class, () ->
                trainerService.createTrainerProfile(null, "Smith", 1L));
    }
}