package com.company.gym.dao;

import com.company.gym.entity.Trainee;

public interface TraineeDAO extends BaseDAO <Trainee> {
    void update(Trainee trainee);
    void delete(Long id);
}
