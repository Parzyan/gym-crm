package com.company.gym_crm.service;

import com.company.gym_crm.dao.TrainingDAO;
import com.company.gym_crm.dao.impl.TrainingDAOImpl;
import com.company.gym_crm.entity.Training;
import com.company.gym_crm.entity.TrainingType;
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
        trainingService = new TrainingService(trainingDAO);
    }

    @Test
    void createTraining() {
        TrainingType specialization = new TrainingType();
        specialization.setName("Yoga");
        assertThrows(IllegalArgumentException.class, () -> {
            trainingService.createTraining(1L, 1L, "Yoga", specialization, LocalDate.now(), -10);
        });
    }

    @Test
    void getAllTrainings() {
        assertTrue(trainingService.getAllTrainings().isEmpty());
    }
}
