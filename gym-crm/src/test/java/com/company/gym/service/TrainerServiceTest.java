package com.company.gym.service;

import com.company.gym.dao.BaseAndUpdateDAO;
import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.impl.TraineeDAOImpl;
import com.company.gym.dao.impl.TrainerDAOImpl;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.TrainingType;
import com.company.gym.util.PasswordGenerator;
import com.company.gym.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    private TrainerService trainerService;
    private TraineeDAO traineeDAO;
    private BaseAndUpdateDAO<Trainer> trainerDAO;
    private UserService userService;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private Map<Long, Trainee> storage1;
    private Map<Long, Trainer> storage2;

    @BeforeEach
    void setUp() {
        storage1 = new HashMap<>();
        storage2 = new HashMap<>();
        traineeDAO = new TraineeDAOImpl(storage1);
        trainerDAO = new TrainerDAOImpl(storage2);
        userService = new UserService();
        userService.setTraineeDAO(traineeDAO);
        userService.setTrainerDAO(trainerDAO);

        usernameGenerator = new UsernameGenerator(userService);
        passwordGenerator = mock(PasswordGenerator.class);

        trainerService = new TrainerService();
        trainerService.setTrainerDAO(trainerDAO);
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void createTrainer() {
        when(passwordGenerator.generatePassword()).thenReturn("1234567890");

        TrainingType specialization = TrainingType.YOGA;
        Trainer trainer = trainerService.createTrainer("Mike", "Johnson", specialization);

        assertEquals("YOGA", trainer.getSpecialization().name());
        assertEquals("Mike.Johnson", trainer.getUsername());
        verify(passwordGenerator).generatePassword();
    }

    @Test
    void createTrainerDuplicateUsername() {
        when(passwordGenerator.generatePassword()).thenReturn("1234567890");

        TrainingType specialization = TrainingType.YOGA;
        trainerService.createTrainer("Mike", "Johnson", specialization);
        Trainer trainerDuplicate = trainerService.createTrainer("Mike", "Johnson", specialization);

        assertEquals("Mike.Johnson1", trainerDuplicate.getUsername());
        assertEquals("1234567890", trainerDuplicate.getPassword());
        assertEquals(2, storage2.size());
    }

    @Test
    void updateTrainer() {
        Trainer trainer = new Trainer();
        trainer.setIsActive(false);
        storage2.put(1L, trainer);

        trainerService.updateTrainer(1L, true, null);
        assertTrue(trainer.getIsActive());
    }

    @Test
    void getTrainer() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        storage2.put(1L, trainer);

        Optional<Trainer> result = trainerService.getById(1L);
        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void getTrainer_NotFound() {
        Optional<Trainer> result = trainerService.getById(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllTrainees() {
        Trainer trainer = new Trainer();
        storage2.put(1L, trainer);
        storage2.put(2L, trainer);
        storage2.put(3L, trainer);

        assertEquals(3, trainerService.getAll().size());
    }
}