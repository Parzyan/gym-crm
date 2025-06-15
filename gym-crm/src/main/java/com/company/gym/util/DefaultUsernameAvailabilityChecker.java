package com.company.gym.util;

import com.company.gym.service.impl.UserServiceImpl;
import org.springframework.stereotype.Component;

@Component
public class DefaultUsernameAvailabilityChecker implements UsernameAvailabilityChecker {

    private final UserServiceImpl userService;

    public DefaultUsernameAvailabilityChecker(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return userService.usernameExists(username);
    }
}
