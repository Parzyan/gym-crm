package com.company.gym.dao.impl;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.entity.Trainee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Transactional
public class TraineeDAOImpl implements TraineeDAO {
    private static final Logger logger = LoggerFactory.getLogger(TraineeDAOImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Trainee trainee) {
        try {
            entityManager.persist(trainee);
            logger.info("Saved new trainee with ID: {}", trainee.getId());
        } catch (Exception e) {
            logger.error("Error saving trainee", e);
            throw e;
        }
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        Trainee trainee = entityManager.find(Trainee.class, id);
        return Optional.ofNullable(trainee);
    }

    @Override
    public List<Trainee> findAll() {
        TypedQuery<Trainee> query = entityManager.createQuery("FROM Trainee", Trainee.class);
        return query.getResultList();
    }

    @Override
    public void update(Trainee trainee) {
        try {
            entityManager.merge(trainee);
            logger.debug("Updated trainee with ID: {}", trainee.getId());
        } catch (Exception e) {
            logger.error("Error updating trainee with ID: {}", trainee.getId(), e);
            throw e;
        }
    }

    @Override
    public void delete(Long id) {
        try {
            Trainee trainee = entityManager.find(Trainee.class, id);
            if (trainee != null) {
                entityManager.remove(trainee);
                logger.info("Deleted trainee with ID: {}", id);
            }
        } catch (Exception e) {
            logger.error("Error deleting trainee with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        try {
            TypedQuery<Trainee> query = entityManager.createQuery(
                    "SELECT t FROM Trainee t JOIN t.user u WHERE u.username = :username", Trainee.class);
            query.setParameter("username", username);
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            logger.error("Error finding trainee by username: {}", username, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Trainee> findActiveTrainees() {
        TypedQuery<Trainee> query = entityManager.createQuery(
                "SELECT t FROM Trainee t JOIN t.user u WHERE u.isActive = true", Trainee.class);
        return query.getResultList();
    }

    @Override
    public void changePassword(Long traineeId, String newPassword) {
        Trainee trainee = entityManager.find(Trainee.class, traineeId);
        if (trainee != null && trainee.getUser() != null) {
            trainee.getUser().setPassword(newPassword);
            entityManager.merge(trainee);
            logger.info("Changed password for trainee ID: {}", traineeId);
        }
    }

    @Override
    public void updateActivity(Long traineeId, boolean isActive) {
        Trainee trainee = entityManager.find(Trainee.class, traineeId);
        if (trainee != null && trainee.getUser() != null) {
            trainee.getUser().setIsActive(isActive);
            entityManager.merge(trainee);
            logger.info("Updated activity status for trainee ID: {} to {}", traineeId, isActive);
        }
    }
}
