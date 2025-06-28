package com.company.gym.service;

import com.company.gym.entity.Credentials;

import java.util.Optional;

public interface BaseUserService<T> extends BaseService<T> {
    void updateStatus(Credentials credentials);
    void changePassword(String username, String oldPassword, String newPassword);
    Optional<T> getByUsername(String username);
}
