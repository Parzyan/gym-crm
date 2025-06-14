package com.company.gym.service;

import com.company.gym.entity.Trainer;

import java.util.List;

public interface TrainerService {
    Trainer createTrainerProfile(String firstName, String lastName, Long specializationId);
    boolean authenticateTrainer(String username, String password);
    void changeTrainerPassword(String username, String oldPassword, String newPassword);
    Trainer updateTrainerProfile(String username, Long specializationId);
    void updateTrainerStatus(String username, boolean isActive);
    List<Trainer> getTrainersBySpecialization(Long trainingTypeId);
    List<Trainer> getUnassignedTrainers(String traineeUsername);
}
