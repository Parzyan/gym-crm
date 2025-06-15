package com.company.gym.service;

public interface CrudService<T> extends BaseAndUpdateService<T> {
    void delete(Long id);
}
