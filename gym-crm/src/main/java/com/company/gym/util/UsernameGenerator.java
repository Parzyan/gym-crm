package com.company.gym.util;

import com.company.gym.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UsernameGenerator {
    private static final Logger logger = LoggerFactory.getLogger(UsernameGenerator.class);

    private final UserService userService;

    public UsernameGenerator(UserService userService) {
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