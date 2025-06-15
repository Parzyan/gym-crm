package com.company.gym.service;

import com.company.gym.entity.Trainer;

import java.util.List;

public interface TrainerService {
    Trainer createTrainerProfile(String firstName, String lastName, Long specializationId);
    void changeTrainerPassword(String username, String oldPassword, String newPassword);
    Trainer updateTrainerProfile(String requesterPassword, String username, Long specializationId);
    void updateTrainerStatus(String requesterPassword, String username);
    List<Trainer> getTrainersBySpecialization(Long trainingTypeId);
    List<Trainer> getUnassignedTrainers(String traineeUsername);
}
