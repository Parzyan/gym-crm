package com.company.gym_crm.dao;

import com.company.gym_crm.entity.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingDAO {
    void save(Training training);
    Optional<Training> findById(Long id);
    List<Training> findAll();
}
