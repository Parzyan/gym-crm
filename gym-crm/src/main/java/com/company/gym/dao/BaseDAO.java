package com.company.gym.dao;

import java.util.List;
import java.util.Optional;

public interface BaseDAO<T> {
    void save(T obj);
    Optional<T> findById(Long id);
    List<T> findAll();
}
