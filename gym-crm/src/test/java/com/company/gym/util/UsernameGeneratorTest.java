package com.company.gym.util;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.dao.impl.TraineeDAOImpl;
import com.company.gym.dao.impl.TrainerDAOImpl;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UsernameGeneratorTest {

    private UsernameGenerator usernameGenerator;
    private UserService userService;
    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;
    private Map<Long, Trainee> traineeStorage;
    private Map<Long, Trainer> trainerStorage;

    @BeforeEach
    void setUp() {
        traineeStorage = new HashMap<>();
        trainerStorage = new HashMap<>();
        traineeDAO = new TraineeDAOImpl(traineeStorage);
        trainerDAO = new TrainerDAOImpl(trainerStorage);
        userService = new UserService();
        userService.setTraineeDAO(traineeDAO);
        userService.setTrainerDAO(trainerDAO);
        usernameGenerator = new UsernameGenerator(userService);
    }

    @Test
    void generateUsername() {
        String username = usernameGenerator.generateUsername("John", "Smith");
        assertEquals("John.Smith", username);
    }

    @Test
    void generateUsername_WithDuplicate() {
        Trainee existing = new Trainee();
        existing.setUsername("John.Smith");
        traineeStorage.put(1L, existing);

        String username = usernameGenerator.generateUsername("John", "Smith");
        assertEquals("John.Smith1", username);
    }

    @Test
    void generateUsername_WithMultipleDuplicates() {
        Trainee existing1 = new Trainee();
        existing1.setUsername("John.Smith");
        traineeStorage.put(1L, existing1);

        Trainee existing2 = new Trainee();
        existing2.setUsername("John.Smith1");
        traineeStorage.put(2L, existing2);

        Trainee existing3 = new Trainee();
        existing3.setUsername("John.Smith2");
        traineeStorage.put(3L, existing3);

        String username = usernameGenerator.generateUsername("John", "Smith");
        assertEquals("John.Smith3", username);
    }

    @Test
    void generateUsername_WithEmptyNames() {
        String username = usernameGenerator.generateUsername("", "");
        assertEquals(".", username);
    }
}
