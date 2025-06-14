package com.company.gym.dao;

import com.company.gym.entity.TrainingType;

import java.util.Optional;

public interface TrainingTypeDAO {
    Optional<TrainingType> findByName(String name);
    Optional<TrainingType> findById(Long id);
}