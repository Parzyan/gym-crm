package com.company.gym_crm.service;

import com.company.gym_crm.dao.TraineeDAO;
import com.company.gym_crm.dao.TrainerDAO;
import com.company.gym_crm.entity.Trainee;
import com.company.gym_crm.entity.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsernameGenerator {
    private static final Logger logger = LoggerFactory.getLogger(UsernameGenerator.class);

    TraineeDAO traineeDAO;
    TrainerDAO trainerDAO;

    @Autowired
    UsernameGenerator(TraineeDAO traineeDAO, TrainerDAO trainerDAO) {
        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
    }

    public String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int suffix = 1;

        while (usernameExists(username)) {
            username = baseUsername + suffix++;
            logger.debug("Username {} exists, trying {}", baseUsername, username);
        }

        logger.debug("Generated username: {}", username);
        return username;
    }

    private Boolean usernameExists(String username) {
        List<Trainee> trainees = traineeDAO.findAll();
        List<Trainer> trainers = trainerDAO.findAll();
        if(trainees != null) {
            for(Trainee trainee : trainees) {
                if(trainee.getUsername().equals(username)) {
                    return true;
                }
            }
        }
        if(trainers != null) {
            for(Trainer trainer : trainers) {
                if(trainer.getUsername().equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }
}