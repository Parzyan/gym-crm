package com.company.gym.dao.impl;

import com.company.gym.dao.BaseUserDAO;
import com.company.gym.entity.User;
import com.company.gym.entity.UserContainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Transactional
public abstract class BaseUserDAOImpl<T> implements BaseUserDAO<T> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    protected EntityManager entityManager;

    protected abstract Class<T> getEntityClass();

    @Override
    public void changePassword(Long userId, String newPassword) {
        T entity = entityManager.find(getEntityClass(), userId);
        if (entity != null && ((UserContainer) entity).getUser() != null) {
            ((UserContainer) entity).getUser().setPassword(newPassword);
            entityManager.merge(entity);
            logger.info("Changed password for {} ID: {}", getEntityClass().getSimpleName(), userId);
        }
    }

    @Override
    public void updateStatus(Long userId) {
        T entity = entityManager.find(getEntityClass(), userId);
        if (entity != null && ((UserContainer) entity).getUser() != null) {
            User user = ((UserContainer) entity).getUser();
            user.setIsActive(!user.getIsActive());
            entityManager.merge(entity);
            logger.info("Updated activity status for {} ID: {} to {}",
                    getEntityClass().getSimpleName(), userId, user.getIsActive());
        }
    }

    @Override
    public Optional<T> findById(Long id) {
        T entity = entityManager.find(getEntityClass(), id);
        return Optional.ofNullable(entity);
    }

    @Override
    public List<T> findAll() {
        TypedQuery<T> query = entityManager.createQuery(
                "FROM " + getEntityClass().getSimpleName(), getEntityClass());
        return query.getResultList();
    }
}
