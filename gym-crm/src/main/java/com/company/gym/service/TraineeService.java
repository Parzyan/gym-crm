package com.company.gym.service;

import com.company.gym.entity.Credentials;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.dto.request.UpdateTraineeTrainersRequest;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface TraineeService extends BaseUserService<Trainee> {
    Trainee createTraineeProfile(String firstName, String lastName, Date dateOfBirth, String address);
    Trainee updateTraineeProfile(Credentials credentials, Date dateOfBirth, String address);
    void deleteTraineeProfile(Credentials credentials);
    void updateTraineeTrainers(Credentials credentials, Set<Long> trainerIds);
    List<Trainer> getTrainersForTrainee(String traineeUsername);
    List<Trainer> updateTrainingTrainers(Credentials credentials, List<UpdateTraineeTrainersRequest.TrainingTrainerUpdate> updates);
}
