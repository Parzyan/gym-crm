package com.company.gym.service;

public interface FullCrudService<T> extends BaseAndUpdateService<T> {
    void delete(Long id);
}
