package com.company.gym.dao.impl;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.entity.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TraineeDAOImpl implements TraineeDAO {
    private static final Logger logger = LoggerFactory.getLogger(TraineeDAOImpl.class);

    private final Map<Long, Trainee> storage;
    private Long currentId = 1L;

    @Autowired
    public TraineeDAOImpl(@Qualifier("traineeStorage") Map<Long, Trainee> storage) {
        this.storage = storage;
        if (!storage.isEmpty()) {
            currentId = Collections.max(storage.keySet()) + 1;
        }
    }

    @Override
    public void save(Trainee trainee) {
        trainee.setId(currentId++);
        storage.put(trainee.getId(), trainee);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainee> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void update(Trainee trainee) {
        if (storage.containsKey(trainee.getId())) {
            storage.put(trainee.getId(), trainee);
            logger.debug("Updated trainee with ID: {}", trainee.getId());
        } else {
            logger.warn("Attempted to update non-existent trainee with ID: {}", trainee.getId());
        }
    }

    @Override
    public void delete(Long id) {
        storage.remove(id);
    }
}
