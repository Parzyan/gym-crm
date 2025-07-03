package com.company.gym.service.impl;

import com.company.gym.dao.impl.UserDAOImpl;
import com.company.gym.entity.Credentials;
import com.company.gym.entity.User;
import com.company.gym.exception.InactiveUserException;
import com.company.gym.exception.InvalidCredentialsException;
import com.company.gym.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private UserDAOImpl userDAO;

    @Autowired
    public void setUserDao(UserDAOImpl userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void authenticate(Credentials credentials) throws InvalidCredentialsException {
        User user = userDAO.findByUsername(credentials.getUsername())
                .orElseThrow(() -> {
                    logger.warn("Authentication failed - user not found: {}", credentials.getUsername());
                    return new InvalidCredentialsException("Invalid username or password");
                });
        if(!user.getIsActive()) {
            throw new InactiveUserException("User is not active");
        }
    }
}
