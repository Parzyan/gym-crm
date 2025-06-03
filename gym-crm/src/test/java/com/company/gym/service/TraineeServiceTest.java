package com.company.gym.service;

import com.company.gym.dao.BaseAndUpdateDAO;
import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.impl.TraineeDAOImpl;
import com.company.gym.dao.impl.TrainerDAOImpl;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.util.PasswordGenerator;
import com.company.gym.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    private TraineeService traineeService;
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

        traineeService = new TraineeService();
        traineeService.setTraineeDAO(traineeDAO);
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void createTrainee() {
        when(passwordGenerator.generatePassword()).thenReturn("1234567890");

        Trainee trainee = traineeService.createTrainee("John", "Smith", LocalDate.of(1990, 1, 1), "123 Street");

        assertEquals("John.Smith", trainee.getUsername());
        assertEquals("1234567890", trainee.getPassword());
        assertEquals(1, storage1.size());
        verify(passwordGenerator).generatePassword();
    }

    @Test
    void createTraineeDuplicateUsername() {
        when(passwordGenerator.generatePassword()).thenReturn("1234567890");

        traineeService.createTrainee("John", "Smith", LocalDate.of(1990, 1, 1), "123 Street");
        Trainee traineeDuplicate = traineeService.createTrainee("John", "Smith", LocalDate.of(1990, 1, 1), "124 Street");

        assertEquals("John.Smith1", traineeDuplicate.getUsername());
        assertEquals("1234567890", traineeDuplicate.getPassword());
        assertEquals(2, storage1.size());
    }

    @Test
    void updateTrainee() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setIsActive(false);
        storage1.put(1L, trainee);

        Trainee updated = traineeService.updateTrainee(1L, true, "New Address");
        assertTrue(updated.getIsActive());
        assertEquals("New Address", updated.getAddress());
    }

    @Test
    void getTrainee() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        storage1.put(1L, trainee);

        Optional<Trainee> result = traineeService.getById(1L);
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void getTrainee_NotFound() {
        Optional<Trainee> result = traineeService.getById(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllTrainees() {
        Trainee trainee1 = new Trainee();
        trainee1.setId(1L);
        Trainee trainee2 = new Trainee();
        trainee2.setId(2L);
        Trainee trainee3 = new Trainee();
        trainee3.setId(3L);

        storage1.put(1L, trainee1);
        storage1.put(2L, trainee2);
        storage1.put(3L, trainee3);

        assertEquals(3, traineeService.getAll().size());
    }

    @Test
    void deleteTrainee() {
        Trainee trainee1 = new Trainee();
        trainee1.setId(1L);
        Trainee trainee2 = new Trainee();
        trainee2.setId(2L);
        Trainee trainee3 = new Trainee();
        trainee3.setId(3L);

        storage1.put(1L, trainee1);
        storage1.put(2L, trainee2);
        storage1.put(3L, trainee3);

        traineeService.delete(1L);
        assertEquals(2, traineeService.getAll().size());
        assertFalse(storage1.containsKey(1L));
    }
}