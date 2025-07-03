package com.company.gym.service.impl;

import com.company.gym.dao.impl.UserDAOImpl;
import com.company.gym.entity.User;
import com.company.gym.service.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserDAOImpl userDAO;

    @Autowired
    public void setUserDAO(UserDAOImpl userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public boolean usernameExists(String username) {
        try {
            boolean exists = userDAO.findByUsername(username).isPresent();
            if (exists) {
                logger.debug("Username '{}' already exists in the system", username);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error checking username existence: {}", username, e);
            throw new RuntimeException("Error checking username existence", e);
        }
    }

    @Override
    public Optional<User> getByUsername(String username) {
        return userDAO.findByUsername(username);
    }
}
