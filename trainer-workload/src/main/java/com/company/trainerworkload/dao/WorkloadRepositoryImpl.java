package com.company.trainerworkload.dao;

import com.company.trainerworkload.entity.TrainerSummary;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class WorkloadRepositoryImpl {
    private static final Map<String, TrainerSummary> database = new ConcurrentHashMap<>();

    public TrainerSummary save(TrainerSummary summary) {
        database.put(summary.getTrainerUsername(), summary);
        return summary;
    }

    public Optional<TrainerSummary> findByUsername(String username) {
        return Optional.ofNullable(database.get(username));
    }
}
