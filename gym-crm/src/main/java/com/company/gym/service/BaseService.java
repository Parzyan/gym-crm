package com.company.gym.service;

import java.util.List;
import java.util.Optional;

public interface BaseService<T> {
    T create(T entity);
    Optional<T> getById(Long id);
    List<T> getAll();
}
