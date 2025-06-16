package com.company.gym.service;

import com.company.gym.entity.Credentials;
import com.company.gym.entity.Trainee;

import java.util.Date;
import java.util.Set;

public interface TraineeService extends BaseUserService<Trainee> {
    Trainee createTraineeProfile(String firstName, String lastName, Date dateOfBirth, String address);
    Trainee updateTraineeProfile(Credentials credentials, Date dateOfBirth, String address);
    void deleteTraineeProfile(Credentials credentials);
    void updateTraineeTrainers(Credentials credentials, Set<Long> trainerIds);
}
