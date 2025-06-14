package com.company.gym.dao.impl;

import com.company.gym.dao.BaseAndUpdateDAO;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.entity.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Transactional
public class TrainerDAOImpl implements TrainerDAO {
    private static final Logger logger = LoggerFactory.getLogger(TrainerDAOImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Trainer trainer) {
        try {
            entityManager.persist(trainer);
            logger.info("Saved new trainer with ID: {}", trainer.getId());
        } catch (Exception e) {
            logger.error("Error saving trainer", e);
            throw e;
        }
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Trainer trainer = entityManager.find(Trainer.class, id);
        return Optional.ofNullable(trainer);
    }

    @Override
    public List<Trainer> findAll() {
        TypedQuery<Trainer> query = entityManager.createQuery("FROM Trainer", Trainer.class);
        return query.getResultList();
    }

    @Override
    public void update(Trainer trainer) {
        try {
            entityManager.merge(trainer);
            logger.debug("Updated trainer with ID: {}", trainer.getId());
        } catch (Exception e) {
            logger.error("Error updating trainer with ID: {}", trainer.getId(), e);
            throw e;
        }
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        try {
            TypedQuery<Trainer> query = entityManager.createQuery(
                    "SELECT t FROM Trainer t JOIN t.user u WHERE u.username = :username", Trainer.class);
            query.setParameter("username", username);
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            logger.error("Error finding trainer by username: {}", username, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Trainer> findBySpecialization(Long trainingTypeId) {
        TypedQuery<Trainer> query = entityManager.createQuery(
                "SELECT t FROM Trainer t WHERE t.specialization.id = :trainingTypeId", Trainer.class);
        query.setParameter("trainingTypeId", trainingTypeId);
        return query.getResultList();
    }

    @Override
    public void changePassword(Long trainerId, String newPassword) {
        Trainer trainer = entityManager.find(Trainer.class, trainerId);
        if (trainer != null && trainer.getUser() != null) {
            trainer.getUser().setPassword(newPassword);
            entityManager.merge(trainer);
            logger.info("Changed password for trainer ID: {}", trainerId);
        }
    }

    @Override
    public void updateActivity(Long trainerId, boolean isActive) {
        Trainer trainer = entityManager.find(Trainer.class, trainerId);
        if (trainer != null && trainer.getUser() != null) {
            trainer.getUser().setIsActive(isActive);
            entityManager.merge(trainer);
            logger.info("Updated activity status for trainer ID: {} to {}", trainerId, isActive);
        }
    }

    @Override
    public List<Trainer> findTrainersNotAssignedToTrainee(Long traineeId) {
        TypedQuery<Trainer> query = entityManager.createQuery(
                "SELECT t FROM Trainer t WHERE t.id NOT IN " +
                        "(SELECT tr.trainer.id FROM Training tr WHERE tr.trainee.id = :traineeId)", Trainer.class);
        query.setParameter("traineeId", traineeId);
        return query.getResultList();
    }
}
