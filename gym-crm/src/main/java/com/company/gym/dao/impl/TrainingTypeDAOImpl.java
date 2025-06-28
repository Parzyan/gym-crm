package com.company.gym.dao.impl;

import com.company.gym.dao.TrainingTypeDAO;
import com.company.gym.entity.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class TrainingTypeDAOImpl implements TrainingTypeDAO {
    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeDAOImpl.class);

    public static final String FIND_BY_NAME_QUERY =
            "SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TrainingType> findByName(String name) {
        try {
            TypedQuery<TrainingType> query = entityManager.createQuery(FIND_BY_NAME_QUERY, TrainingType.class);
            query.setParameter("name", name);
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            logger.error("Error finding training type by name: {}", name, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<TrainingType> findById(Long id) {
        TrainingType trainingType = entityManager.find(TrainingType.class, id);
        return Optional.ofNullable(trainingType);
    }

    @Override
    public List<TrainingType> findAll() {
        return entityManager.createQuery("FROM TrainingType", TrainingType.class).getResultList();
    }
}
