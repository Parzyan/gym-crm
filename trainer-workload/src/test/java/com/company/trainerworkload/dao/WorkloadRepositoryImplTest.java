package com.company.trainerworkload.dao;

import com.company.trainerworkload.entity.TrainerSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkloadRepositoryImplTest {
    private WorkloadRepositoryImpl workloadRepository;

    @BeforeEach
    void setUp() throws Exception {
        workloadRepository = new WorkloadRepositoryImpl();
        Field databaseField = WorkloadRepositoryImpl.class.getDeclaredField("database");
        databaseField.setAccessible(true);
        Map<String, TrainerSummary> database = (Map<String, TrainerSummary>) databaseField.get(null);
        database.clear();
    }

    @Test
    void findByUsername_whenUserDoesNotExist() {
        Optional<TrainerSummary> result = workloadRepository.findByUsername("non.existent.user");
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_whenUserExists() {
        TrainerSummary summaryToSave = new TrainerSummary();
        summaryToSave.setTrainerUsername("john.doe");
        summaryToSave.setTrainerFirstName("John");

        workloadRepository.save(summaryToSave);
        Optional<TrainerSummary> result = workloadRepository.findByUsername("john.doe");

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getTrainerFirstName());
    }

    @Test
    void save_whenUpdatingExistingUser() {
        TrainerSummary initialSummary = new TrainerSummary();
        initialSummary.setTrainerUsername("jane.doe");
        initialSummary.setTrainerFirstName("Jane");
        workloadRepository.save(initialSummary);

        TrainerSummary updatedSummary = new TrainerSummary();
        updatedSummary.setTrainerUsername("jane.doe");
        updatedSummary.setTrainerFirstName("Janet");

        workloadRepository.save(updatedSummary);
        Optional<TrainerSummary> result = workloadRepository.findByUsername("jane.doe");

        assertTrue(result.isPresent());
        assertEquals("Janet", result.get().getTrainerFirstName());
    }
}
