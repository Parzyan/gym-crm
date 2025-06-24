package com.company.gym.service;

import com.company.gym.entity.Credentials;
import com.company.gym.entity.Trainer;

import java.util.List;

public interface TrainerService extends BaseUserService<Trainer> {
    Trainer createTrainerProfile(String firstName, String lastName, Long specializationId);
    Trainer updateTrainerProfile(Credentials credentials, Long specializationId);
    List<Trainer> getTrainersBySpecialization(Long trainingTypeId);
    List<Trainer> getUnassignedTrainers(String traineeUsername);
}
