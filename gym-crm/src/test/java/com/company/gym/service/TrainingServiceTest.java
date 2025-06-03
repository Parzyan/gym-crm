package com.company.gym.service;

import com.company.gym.dao.TrainingDAO;
import com.company.gym.dao.impl.TrainingDAOImpl;
import com.company.gym.entity.Training;
import com.company.gym.entity.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrainingServiceTest {
    private TrainingService trainingService;
    private TrainingDAO trainingDAO;
    private Map<Long, Training> storage;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainingDAO = new TrainingDAOImpl(storage);
        trainingService = new TrainingService();
        trainingService.setTrainingDAO(trainingDAO);
    }

    @Test
    void createTraining() {
        TrainingType specialization = TrainingType.Yoga;
        assertThrows(IllegalArgumentException.class, () -> {
            trainingService.createTraining(1L, 1L, "Yoga", specialization, LocalDate.now(), -10);
        });
    }

    @Test
    void getAllTrainings() {
        assertTrue(trainingService.getAll().isEmpty());
    }
}
