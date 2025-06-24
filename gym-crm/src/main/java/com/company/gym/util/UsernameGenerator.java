package com.company.gym.util;

import com.company.gym.service.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class UsernameGenerator {

    private static final Logger logger = LoggerFactory.getLogger(UsernameGenerator.class);

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int suffix = 1;

        while (userService.usernameExists(username)) {
            username = baseUsername + suffix++;
            logger.debug("Username {} exists, trying {}", baseUsername, username);
        }

        logger.debug("Generated username: {}", username);
        return username;
    }
}
