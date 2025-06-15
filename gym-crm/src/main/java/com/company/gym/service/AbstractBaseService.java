package com.company.gym.service;

import com.company.gym.dao.BaseDAO;

import java.util.List;
import java.util.Optional;

public abstract class AbstractBaseService<T> implements BaseService<T> {
    protected BaseDAO<T> dao;

    public void setDao(BaseDAO<T> dao) {
        this.dao = dao;
    }

    @Override
    public T create(T entity) {
        dao.save(entity);
        return entity;
    }

    @Override
    public Optional<T> getById(Long id) {
        return dao.findById(id);
    }

    @Override
    public List<T> getAll() {
        return dao.findAll();
    }
}
