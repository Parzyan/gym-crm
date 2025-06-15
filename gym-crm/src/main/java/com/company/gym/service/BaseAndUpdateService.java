package com.company.gym.service;

import java.util.Optional;

public interface BaseAndUpdateService<T> extends BaseService<T> {
    T update(T entity);
    Optional<T> getByUsername(String username);
}
