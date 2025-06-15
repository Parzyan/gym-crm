package com.company.gym.dao;

import java.util.Optional;

public interface BaseAndUpdateDAO<T> extends BaseDAO<T> {
    void update(T entity);
    Optional<T> findByUsername(String username);
    void changePassword(Long id, String newPassword);
    void updateActivity(Long id, boolean isActive);
}
