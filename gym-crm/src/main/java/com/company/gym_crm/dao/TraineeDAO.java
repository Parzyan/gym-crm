package com.company.gym_crm.dao;

import com.company.gym_crm.entity.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDAO {
    public void save(Trainee trainee);
    Optional<Trainee> findById(Long id);
    List<Trainee> findAll();
    void update(Trainee trainee);
    void delete(Long id);
}
