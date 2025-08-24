package com.company.trainerworkload.dao;

import com.company.trainerworkload.entity.TrainerSummary;

import java.util.Optional;

public interface WorkloadRepository {
    TrainerSummary save(TrainerSummary summary);
    Optional<TrainerSummary> findByUsername(String username);
}
