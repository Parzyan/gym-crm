package com.company.gym.service;

public interface BaseAndUpdateService<T> extends BaseService<T> {
    T update(T entity);
}