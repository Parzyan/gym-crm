package com.company.gym.service;

import com.company.gym.entity.Trainee;

import java.util.Date;
import java.util.List;

public interface TraineeService extends CrudService<Trainee>{
    Trainee createTraineeProfile(String firstName, String lastName, Date dateOfBirth, String address);
    void changeTraineePassword(String username, String oldPassword, String newPassword);
    Trainee updateTraineeProfile(String requesterPassword, String username, Date dateOfBirth, String address);
    void updateTraineeStatus(String requesterPassword, String username);
    void deleteTraineeProfile(String requesterPassword, String username);
    List<Trainee> getActiveTrainees();
}
