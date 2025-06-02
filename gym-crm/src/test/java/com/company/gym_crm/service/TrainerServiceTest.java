package com.company.gym_crm.service;

import com.company.gym_crm.dao.TraineeDAO;
import com.company.gym_crm.dao.TrainerDAO;
import com.company.gym_crm.dao.impl.TraineeDAOImpl;
import com.company.gym_crm.dao.impl.TrainerDAOImpl;
import com.company.gym_crm.entity.Trainee;
import com.company.gym_crm.entity.Trainer;
import com.company.gym_crm.entity.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    private TrainerService trainerService;
    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;
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
        usernameGenerator = new UsernameGenerator(traineeDAO, trainerDAO);
        passwordGenerator = mock(PasswordGenerator.class);
        trainerService = new TrainerService(trainerDAO, usernameGenerator, passwordGenerator);
    }

    @Test
    void createTrainer() {
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("1234567890");

        TrainingType specialization = new TrainingType();
        specialization.setName("Yoga");
        Trainer trainer = trainerService.createTrainer("Mike", "Johnson", specialization);

        assertEquals("Yoga", trainer.getSpecialization().getName());
        assertEquals("Mike.Johnson", trainer.getUsername());
        verify(passwordGenerator).generatePassword(10);
    }

    @Test
    void createTrainerDuplicateUsername() {
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("1234567890");

        TrainingType specialization = new TrainingType();
        specialization.setName("Yoga");
        Trainer trainer = trainerService.createTrainer("Mike", "Johnson", specialization);
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
    void getTrainee() {
        Trainer trainer = new Trainer();
        storage2.put(1L, trainer);

        assertEquals(trainer, trainerService.getTrainer(1L));
    }

    @Test
    void getTrainee_NotFound() {
        assertNull(trainerService.getTrainer(999L));
    }

    @Test
    void getAllTrainees() {
        Trainer trainer = new Trainer();
        storage2.put(1L, trainer);
        storage2.put(2L, trainer);
        storage2.put(3L, trainer);

        assertEquals(3, trainerService.getAllTrainers().size());
    }
}