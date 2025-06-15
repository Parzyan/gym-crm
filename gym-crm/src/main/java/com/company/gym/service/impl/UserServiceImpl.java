package com.company.gym.service.impl;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.service.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

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

    @Override
    public boolean usernameExists(String username) {
        try {
            boolean existsInTrainees = traineeDAO.findByUsername(username).isPresent();
            boolean existsInTrainers = trainerDAO.findByUsername(username).isPresent();

            if (existsInTrainees || existsInTrainers) {
                logger.debug("Username '{}' already exists in the system", username);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error checking username existence: {}", username, e);
            throw new RuntimeException("Error checking username existence", e);
        }
    }
}
