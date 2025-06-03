package com.company.gym.dao;

public interface BaseAndUpdateDAO<T> extends BaseDAO<T> {
    void update(T entity);
}
