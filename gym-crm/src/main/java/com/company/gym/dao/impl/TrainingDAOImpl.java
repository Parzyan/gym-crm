package com.company.gym.dao.impl;

import com.company.gym.dao.TrainingDAO;
import com.company.gym.entity.Training;
import com.company.gym.exception.DAOException;
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

    private static final String FIND_ALL_QUERY = "FROM Training";
    private static final String TRAINEE_CRITERIA_BASE_QUERY =
            "SELECT t FROM Training t WHERE t.trainee.id = :traineeId";
    private static final String TRAINER_CRITERIA_BASE_QUERY =
            "SELECT t FROM Training t WHERE t.trainer.id = :trainerId";
    private static final String DATE_FROM_CLAUSE = " AND t.trainingDate >= :fromDate";
    private static final String DATE_TO_CLAUSE = " AND t.trainingDate <= :toDate";
    private static final String TRAINING_TYPE_CLAUSE = " AND t.trainingType.id = :trainingTypeId";
    private static final String TRAINER_USERNAME_CLAUSE = " AND t.trainer.user.username = :trainerUsername";
    private static final String TRAINEE_USERNAME_CLAUSE = " AND t.trainee.user.username = :traineeUsername";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Training training) {
        try {
            entityManager.persist(training);
            logger.info("Saved new training with ID: {}", training.getId());
        } catch (Exception e) {
            logger.error("Error saving training", e);
            throw new DAOException("Error saving training", e);
        }
    }

    @Override
    public Optional<Training> findById(Long id) {
        Training training = entityManager.find(Training.class, id);
        return Optional.ofNullable(training);
    }

    @Override
    public List<Training> findAll() {
        TypedQuery<Training> query = entityManager.createQuery(FIND_ALL_QUERY, Training.class);
        return query.getResultList();
    }

    @Override
    public List<Training> findTrainingsByTraineeAndCriteria(Long traineeId, Date fromDate,
                                                            Date toDate, String trainerUsername,
                                                            Long trainingTypeId) {
        String queryString = buildTraineeCriteriaQuery(fromDate, toDate, trainerUsername, trainingTypeId);
        TypedQuery<Training> query = entityManager.createQuery(queryString, Training.class);
        setTraineeCriteriaParameters(query, traineeId, fromDate, toDate, trainerUsername, trainingTypeId);
        return query.getResultList();
    }

    @Override
    public List<Training> findTrainingsByTrainerAndCriteria(Long trainerId, Date fromDate,
                                                            Date toDate, String traineeUsername) {
        String queryString = buildTrainerCriteriaQuery(fromDate, toDate, traineeUsername);
        TypedQuery<Training> query = entityManager.createQuery(queryString, Training.class);
        setTrainerCriteriaParameters(query, trainerId, fromDate, toDate, traineeUsername);
        return query.getResultList();
    }

    private String buildTraineeCriteriaQuery(Date fromDate, Date toDate,
                                             String trainerUsername, Long trainingTypeId) {
        StringBuilder jpql = new StringBuilder(TRAINEE_CRITERIA_BASE_QUERY);

        if (fromDate != null) jpql.append(DATE_FROM_CLAUSE);
        if (toDate != null) jpql.append(DATE_TO_CLAUSE);
        if (trainerUsername != null && !trainerUsername.isEmpty()) jpql.append(TRAINER_USERNAME_CLAUSE);
        if (trainingTypeId != null) jpql.append(TRAINING_TYPE_CLAUSE);

        return jpql.toString();
    }

    private void setTraineeCriteriaParameters(TypedQuery<Training> query, Long traineeId,
                                              Date fromDate, Date toDate,
                                              String trainerUsername, Long trainingTypeId) {
        query.setParameter("traineeId", traineeId);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (trainerUsername != null && !trainerUsername.isEmpty()) {
            query.setParameter("trainerUsername", trainerUsername);
        }
        if (trainingTypeId != null) query.setParameter("trainingTypeId", trainingTypeId);
    }

    private String buildTrainerCriteriaQuery(Date fromDate, Date toDate, String traineeUsername) {
        StringBuilder jpql = new StringBuilder(TRAINER_CRITERIA_BASE_QUERY);

        if (fromDate != null) jpql.append(DATE_FROM_CLAUSE);
        if (toDate != null) jpql.append(DATE_TO_CLAUSE);
        if (traineeUsername != null && !traineeUsername.isEmpty()) jpql.append(TRAINEE_USERNAME_CLAUSE);

        return jpql.toString();
    }

    private void setTrainerCriteriaParameters(TypedQuery<Training> query, Long trainerId,
                                              Date fromDate, Date toDate, String traineeUsername) {
        query.setParameter("trainerId", trainerId);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (traineeUsername != null && !traineeUsername.isEmpty()) {
            query.setParameter("traineeUsername", traineeUsername);
        }
    }

    @Override
    public void update(Training training) {
        try {
            entityManager.merge(training);
            logger.debug("Updated training with ID: {}", training.getId());
        } catch (Exception e) {
            logger.error("Error updating trainee with ID: {}", training.getId(), e);
            throw new DAOException("Error updating trainee", e);
        }
    }
}
