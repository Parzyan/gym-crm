package com.company.gym.service;

import com.company.gym.entity.Trainee;

import java.util.Date;
import java.util.List;

public interface TraineeService extends CrudService<Trainee>{
    Trainee createTraineeProfile(String firstName, String lastName, Date dateOfBirth, String address);
    boolean authenticateTrainee(String username, String password);
    void changeTraineePassword(String username, String oldPassword, String newPassword);
    Trainee updateTraineeProfile(String username, Date dateOfBirth, String address);
    void updateTraineeStatus(String username, boolean isActive);
    void deleteTraineeProfile(String username);
    List<Trainee> getActiveTrainees();
}