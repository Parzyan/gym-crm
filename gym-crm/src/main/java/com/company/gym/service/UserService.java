package com.company.gym.service;

import com.company.gym.entity.User;

import java.util.Optional;

public interface UserService {
    boolean usernameExists(String username);
    Optional<User> getByUsername(String username);
}
