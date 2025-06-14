package com.company.gym.service;

import com.company.gym.dao.BaseDAO;
import com.company.gym.dao.impl.TrainingDAOImpl;
import com.company.gym.entity.Training;
import com.company.gym.entity.TrainingType;
import com.company.gym.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrainingServiceImplTest {
    /*private TrainingServiceImpl trainingServiceImpl;
    private BaseDAO<Training> trainingDAO;
    private Map<Long, Training> storage;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainingDAO = new TrainingDAOImpl(storage);
        trainingServiceImpl = new TrainingServiceImpl();
        trainingServiceImpl.setTrainingDAO(trainingDAO);
    }

    @Test
    void createTraining() {
        TrainingType specialization = TrainingType.YOGA;
        assertThrows(IllegalArgumentException.class, () ->
                trainingServiceImpl.createTraining(1L, 1L, "Yoga", specialization, LocalDate.now(), -10));
    }

    @Test
    void getAllTrainings() {
        assertTrue(trainingServiceImpl.getAll().isEmpty());
    }*/
}
