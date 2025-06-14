package com.company.gym.dao;

import com.company.gym.entity.Trainee;

import java.util.List;

public interface TraineeDAO extends BaseAndUpdateDAO<Trainee> {
    void delete(Long id);
    List<Trainee> findActiveTrainees();
}
