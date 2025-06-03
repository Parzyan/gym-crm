package com.company.gym.service;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    public boolean usernameExists(String username) {
        List<Trainee> trainees = traineeDAO.findAll();
        List<Trainer> trainers = trainerDAO.findAll();

        if (trainees != null) {
            for (Trainee trainee : trainees) {
                if (trainee.getUsername().equalsIgnoreCase(username)) {
                    return true;
                }
            }
        }

        if (trainers != null) {
            for (Trainer trainer : trainers) {
                if (trainer.getUsername().equalsIgnoreCase(username)) {
                    return true;
                }
            }
        }
        return false;
    }
}
