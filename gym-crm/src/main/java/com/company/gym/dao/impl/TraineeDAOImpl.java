package com.company.gym.dao.impl;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.entity.Trainee;
import com.company.gym.exception.DAOException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public class TraineeDAOImpl extends BaseUserDAOImpl<Trainee> implements TraineeDAO {
    private static final Logger logger = LoggerFactory.getLogger(TraineeDAOImpl.class);

    private static final String FIND_BY_USERNAME = "SELECT t FROM Trainee t JOIN t.user u WHERE u.username = :username";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected Class<Trainee> getEntityClass() {
        return Trainee.class;
    }

    @Override
    public void save(Trainee trainee) {
        try {
            entityManager.persist(trainee);
            logger.info("Saved new trainee with ID: {}", trainee.getId());
        } catch (Exception e) {
            logger.error("Error saving trainee", e);
            throw new DAOException("Error saving trainee", e);
        }
    }

    @Override
    public void update(Trainee trainee) {
        try {
            entityManager.merge(trainee);
            logger.debug("Updated trainee with ID: {}", trainee.getId());
        } catch (Exception e) {
            logger.error("Error updating trainee with ID: {}", trainee.getId(), e);
            throw new DAOException("Error updating trainee", e);
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
            throw new DAOException("Error deleting trainee", e);
        }
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        try {
            TypedQuery<Trainee> query = entityManager.createQuery(
                    FIND_BY_USERNAME, Trainee.class);
            query.setParameter("username", username);
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            logger.error("Error finding trainee by username: {}", username, e);
            return Optional.empty();
        }
    }
}
