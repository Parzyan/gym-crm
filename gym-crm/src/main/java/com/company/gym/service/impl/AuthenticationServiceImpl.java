package com.company.gym.service.impl;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Autowired
    private TraineeDAO traineeDAO;

    @Autowired
    private TrainerDAO trainerDAO;

    public boolean authenticateUser(String username, String password) {
        boolean authenticated = traineeDAO.findByUsername(username)
                .filter(t -> t.getUser().getPassword().equals(password))
                .isPresent();

        if (!authenticated) {
            authenticated = trainerDAO.findByUsername(username)
                    .filter(t -> t.getUser().getPassword().equals(password))
                    .isPresent();
        }
        else return true;

        if (!authenticated) {
            logger.info("Authentication failed");
            return false;
        }
        else return true;
    }
}
