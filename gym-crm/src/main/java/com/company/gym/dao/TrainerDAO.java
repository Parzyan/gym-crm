package com.company.gym.dao;

import com.company.gym.entity.Trainer;

import java.util.List;

public interface TrainerDAO extends BaseUserDAO<Trainer> {
    List<Trainer> findBySpecialization(Long trainingTypeId);
    List<Trainer> findTrainersNotAssignedToTrainee(Long traineeId);
}
