package com.company.gym.dao;

import com.company.gym.entity.Trainee;

public interface TraineeDAO extends BaseUserDAO<Trainee> {
    void delete(Long id);
}
