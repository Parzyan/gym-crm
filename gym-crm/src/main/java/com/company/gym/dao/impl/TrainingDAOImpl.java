package com.company.gym.dao.impl;

import com.company.gym.dao.TrainingDAO;
import com.company.gym.entity.Training;
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
public class TrainingDAOImpl implements TrainingDAO {
    private static final Logger logger = LoggerFactory.getLogger(TrainingDAOImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Training training) {
        try {
            entityManager.persist(training);
            logger.info("Saved new training with ID: {}", training.getId());
        } catch (Exception e) {
            logger.error("Error saving training", e);
            throw e;
        }
    }

    @Override
    public Optional<Training> findById(Long id) {
        Training training = entityManager.find(Training.class, id);
        return Optional.ofNullable(training);
    }

    @Override
    public List<Training> findAll() {
        TypedQuery<Training> query = entityManager.createQuery("FROM Training", Training.class);
        return query.getResultList();
    }

    @Override
    public List<Training> findTrainingsByTraineeAndCriteria(Long traineeId, Date fromDate,
                                                            Date toDate, String trainerName,
                                                            Long trainingTypeId) {
        StringBuilder jpql = new StringBuilder(
                "SELECT t FROM Training t WHERE t.trainee.id = :traineeId");

        if (fromDate != null) {
            jpql.append(" AND t.trainingDate >= :fromDate");
        }
        if (toDate != null) {
            jpql.append(" AND t.trainingDate <= :toDate");
        }
        if (trainerName != null && !trainerName.isEmpty()) {
            jpql.append(" AND CONCAT(t.trainer.user.firstName, ' ', t.trainer.user.lastName) LIKE :trainerName");
        }
        if (trainingTypeId != null) {
            jpql.append(" AND t.trainingType.id = :trainingTypeId");
        }

        TypedQuery<Training> query = entityManager.createQuery(jpql.toString(), Training.class);
        query.setParameter("traineeId", traineeId);

        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        if (trainerName != null && !trainerName.isEmpty()) {
            query.setParameter("trainerName", "%" + trainerName + "%");
        }
        if (trainingTypeId != null) {
            query.setParameter("trainingTypeId", trainingTypeId);
        }

        return query.getResultList();
    }

    @Override
    public List<Training> findTrainingsByTrainerAndCriteria(Long trainerId, Date fromDate,
                                                            Date toDate, String traineeName) {
        StringBuilder jpql = new StringBuilder(
                "SELECT t FROM Training t WHERE t.trainer.id = :trainerId");

        if (fromDate != null) {
            jpql.append(" AND t.trainingDate >= :fromDate");
        }
        if (toDate != null) {
            jpql.append(" AND t.trainingDate <= :toDate");
        }
        if (traineeName != null && !traineeName.isEmpty()) {
            jpql.append(" AND CONCAT(t.trainee.user.firstName, ' ', t.trainee.user.lastName) LIKE :traineeName");
        }

        TypedQuery<Training> query = entityManager.createQuery(jpql.toString(), Training.class);
        query.setParameter("trainerId", trainerId);

        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        if (traineeName != null && !traineeName.isEmpty()) {
            query.setParameter("traineeName", "%" + traineeName + "%");
        }

        return query.getResultList();
    }
}
