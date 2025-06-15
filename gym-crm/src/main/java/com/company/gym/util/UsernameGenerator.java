package com.company.gym.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UsernameGenerator {

    private static final Logger logger = LoggerFactory.getLogger(UsernameGenerator.class);

    private final UsernameAvailabilityChecker availabilityChecker;

    public UsernameGenerator(UsernameAvailabilityChecker availabilityChecker) {
        this.availabilityChecker = availabilityChecker;
    }

    public String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int suffix = 1;

        while (availabilityChecker.isUsernameTaken(username)) {
            username = baseUsername + suffix++;
            logger.debug("Username {} exists, trying {}", baseUsername, username);
        }

        logger.debug("Generated username: {}", username);
        return username;
    }
}
