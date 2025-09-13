package com.company.trainerworkload.dao;

import com.company.trainerworkload.entity.TrainerSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TrainerSummaryRepository extends MongoRepository<TrainerSummary, String> {
    Optional<TrainerSummary> findByTrainerUsername(String username);
}
