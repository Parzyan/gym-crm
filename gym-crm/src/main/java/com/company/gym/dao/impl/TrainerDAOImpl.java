package com.company.gym.dao.impl;

import com.company.gym.dao.TrainerDAO;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
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
public class TrainerDAOImpl extends BaseUserDAOImpl<Trainer> implements TrainerDAO {
    private static final Logger logger = LoggerFactory.getLogger(TrainerDAOImpl.class);

    private static final String FIND_BY_USERNAME = "SELECT t FROM Trainer t JOIN t.user u WHERE u.username = :username";

    private static final String FIND_BY_SPECIALIZATION_QUERY =
            "SELECT t FROM Trainer t WHERE t.specialization.id = :trainingTypeId";
    private static final String FIND_UNASSIGNED_TRAINERS_QUERY =
            "SELECT t FROM Trainer t WHERE t.id NOT IN " +
                    "(SELECT tr.trainer.id FROM Training tr WHERE tr.trainee.id = :traineeId)";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected Class<Trainer> getEntityClass() {
        return Trainer.class;
    }

    @Override
    public void save(Trainer trainer) {
        try {
            entityManager.persist(trainer);
            logger.info("Saved new trainer with ID: {}", trainer.getId());
        } catch (Exception e) {
            logger.error("Error saving trainer", e);
            throw new DAOException("Error saving trainer", e);
        }
    }

    @Override
    public void update(Trainer trainer) {
        try {
            entityManager.merge(trainer);
            logger.debug("Updated trainer with ID: {}", trainer.getId());
        } catch (Exception e) {
            logger.error("Error updating trainer with ID: {}", trainer.getId(), e);
            throw new DAOException("Error updating trainer", e);
        }
    }

    @Override
    public List<Trainer> findBySpecialization(Long trainingTypeId) {
        try {
            TypedQuery<Trainer> query = entityManager.createQuery(FIND_BY_SPECIALIZATION_QUERY, Trainer.class);
            query.setParameter("trainingTypeId", trainingTypeId);
            return query.getResultList();
        }
        catch (Exception e) {
            logger.error("Error finding trainer by specialization with ID: {}", trainingTypeId, e);
            throw new DAOException("Error finding trainer by specialization with ID", e);
        }
    }

    @Override
    public List<Trainer> findTrainersNotAssignedToTrainee(Long traineeId) {
        TypedQuery<Trainer> query = entityManager.createQuery(FIND_UNASSIGNED_TRAINERS_QUERY, Trainer.class);
        query.setParameter("traineeId", traineeId);
        return query.getResultList();
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        try {
            TypedQuery<Trainer> query = entityManager.createQuery(
                    FIND_BY_USERNAME, Trainer.class);
            query.setParameter("username", username);
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            logger.error("Error finding trainer by username: {}", username, e);
            return Optional.empty();
        }
    }
}
