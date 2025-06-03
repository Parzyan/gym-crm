package com.company.gym.service;

import com.company.gym.dao.BaseDAO;

import java.util.List;
import java.util.Optional;

public abstract class AbstractBaseService <T> {
    protected BaseDAO<T> dao;

    public void setDao(BaseDAO<T> dao) {
        this.dao = dao;
    }

    public Optional<T> getById(Long id) {
        return dao.findById(id);
    }

    public List<T> getAll() {
        return dao.findAll();
    }
}
