package com.company.gym.dao;

import com.company.gym.entity.Trainer;

public interface TrainerDAO extends BaseDAO<Trainer>{
    void update(Trainer trainer);
}
