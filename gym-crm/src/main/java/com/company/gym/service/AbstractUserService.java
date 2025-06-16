package com.company.gym.service;

import com.company.gym.dao.BaseUserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class AbstractUserService<T> extends AbstractBaseService<T> implements BaseUserService<T> {
    protected BaseUserDAO<T> dao;

    public void setDao(BaseUserDAO<T> dao) {
        this.dao = dao;
        super.setDao(dao);
    }

    public Optional<T> getByUsername(String username) {
        return dao.findByUsername(username);
    }
}
