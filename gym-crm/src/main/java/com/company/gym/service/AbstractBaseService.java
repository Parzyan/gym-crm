package com.company.gym.service;

import com.company.gym.dao.BaseDAO;
import com.company.gym.entity.Credentials;
import com.company.gym.exception.InvalidCredentialsException;
import com.company.gym.service.impl.AuthenticationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public abstract class AbstractBaseService<T> implements BaseService<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractBaseService.class);

    protected BaseDAO<T> dao;

    protected AuthenticationServiceImpl authenticationService;

    public void setDao(BaseDAO<T> dao) {
        this.dao = dao;
    }

    @Autowired
    public void setAuthenticationService(AuthenticationServiceImpl authenticationService) {
        this.authenticationService = authenticationService;
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
