package com.company.gym_crm.service;

import com.company.gym_crm.dao.TraineeDAO;
import com.company.gym_crm.dao.TrainerDAO;
import com.company.gym_crm.dao.impl.TraineeDAOImpl;
import com.company.gym_crm.dao.impl.TrainerDAOImpl;
import com.company.gym_crm.entity.Trainee;
import com.company.gym_crm.entity.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    private TraineeService traineeService;
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
        traineeService = new TraineeService(traineeDAO, usernameGenerator, passwordGenerator);
    }

    @Test
    void createTrainee() {
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("1234567890");

        Trainee trainee = traineeService.createTrainee("John", "Smith", LocalDate.of(1990, 1, 1), "123 Street");

        assertEquals("John.Smith", trainee.getUsername());
        assertEquals("1234567890", trainee.getPassword());
        assertEquals(1, storage1.size());
        verify(passwordGenerator).generatePassword(10);
    }

    @Test
    void createTraineeDuplicateUsername() {
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("1234567890");

        Trainee trainee = traineeService.createTrainee("John", "Smith", LocalDate.of(1990, 1, 1), "123 Street");
        Trainee traineeDuplicate = traineeService.createTrainee("John", "Smith", LocalDate.of(1990, 1, 1), "124 Street");

        assertEquals("John.Smith1", traineeDuplicate.getUsername());
        assertEquals("1234567890", traineeDuplicate.getPassword());
        assertEquals(2, storage1.size());
    }

    @Test
    void updateTrainee() {
        Trainee trainee = new Trainee();
        trainee.setIsActive(false);
        storage1.put(1L, trainee);

        traineeService.updateTrainee(1L, true, null);
        assertTrue(trainee.getIsActive());
    }

    @Test
    void getTrainee() {
        Trainee trainee = new Trainee();
        storage1.put(1L, trainee);

        assertEquals(trainee, traineeService.getTrainee(1L));
    }

    @Test
    void getTrainee_NotFound() {
        assertNull(traineeService.getTrainee(999L));
    }

    @Test
    void getAllTrainees() {
        Trainee trainee = new Trainee();
        storage1.put(1L, trainee);
        storage1.put(2L, trainee);
        storage1.put(3L, trainee);

        assertEquals(3, traineeService.getAllTrainees().size());
    }

    @Test
    void deleteTrainee() {
        Trainee trainee = new Trainee();
        storage1.put(1L, trainee);
        storage1.put(2L, trainee);
        storage1.put(3L, trainee);
        traineeService.deleteTrainee(1L);

        assertEquals(2, traineeService.getAllTrainees().size());
    }
}