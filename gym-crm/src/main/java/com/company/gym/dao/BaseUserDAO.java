package com.company.gym.dao;

import com.company.gym.entity.Trainee;

import java.util.Optional;

public interface BaseUserDAO<T> extends BaseDAO<T>{
    void changePassword(Long userId, String newPassword);
    void updateStatus(Long userId);
    Optional<T> findByUsername(String username);
    void update(T entity);
}
