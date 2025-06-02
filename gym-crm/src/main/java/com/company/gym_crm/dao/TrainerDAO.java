package com.company.gym_crm.dao;

import com.company.gym_crm.entity.Trainee;
import com.company.gym_crm.entity.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDAO {
    void save(Trainer trainer);
    Optional<Trainer> findById(Long id);
    List<Trainer> findAll();
    void update(Trainer trainer);
}
