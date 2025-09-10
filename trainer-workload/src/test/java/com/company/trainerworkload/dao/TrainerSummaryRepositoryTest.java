package com.company.trainerworkload.dao;

import com.company.trainerworkload.entity.TrainerSummary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class TrainerSummaryRepositoryTest {

    @Autowired
    private TrainerSummaryRepository repository;

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void findByTrainerUsername_whenUserExists() {
        TrainerSummary summary = new TrainerSummary();
        summary.setTrainerUsername("john.doe");
        repository.save(summary);

        Optional<TrainerSummary> found = repository.findByTrainerUsername("john.doe");

        assertTrue(found.isPresent());
        assertEquals("john.doe", found.get().getTrainerUsername());
    }

    @Test
    void findByTrainerUsername_whenUserDoesNotExist() {
        Optional<TrainerSummary> found = repository.findByTrainerUsername("non.existent");

        assertFalse(found.isPresent());
    }
}
