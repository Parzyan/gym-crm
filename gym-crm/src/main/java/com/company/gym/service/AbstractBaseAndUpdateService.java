package com.company.gym.service;

import com.company.gym.dao.BaseAndUpdateDAO;
import com.company.gym.dao.BaseDAO;

public abstract class AbstractBaseAndUpdateService <T> extends AbstractBaseService <T> implements BaseAndUpdateService <T> {
    protected BaseAndUpdateDAO<T> dao;

    @Override
    public void setDao(BaseDAO<T> dao) {
        super.setDao(dao);
        this.dao = (BaseAndUpdateDAO<T>) dao;
    }

    @Override
    public T update(T entity) {
        dao.update(entity);
        return entity;
    }
}
