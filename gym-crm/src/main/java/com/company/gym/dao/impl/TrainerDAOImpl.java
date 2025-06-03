package com.company.gym.dao.impl;

import com.company.gym.dao.BaseAndUpdateDAO;
import com.company.gym.entity.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TrainerDAOImpl implements BaseAndUpdateDAO<Trainer> {
    private static final Logger logger = LoggerFactory.getLogger(TrainerDAOImpl.class);

    private final Map<Long, Trainer> storage;
    private Long currentId = 1L;

    @Autowired
    public TrainerDAOImpl(@Qualifier("trainerStorage") Map<Long, Trainer> storage) {
        this.storage = storage;
        if (!storage.isEmpty()) {
            currentId = Collections.max(storage.keySet()) + 1;
        }
    }

    @Override
    public void save(Trainer trainer) {
        trainer.setId(currentId++);
        storage.put(trainer.getId(), trainer);
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainer> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void update(Trainer trainer) {
        if (storage.containsKey(trainer.getId())) {
            storage.put(trainer.getId(), trainer);
            logger.debug("Updated trainer with ID: {}", trainer.getId());
        } else {
            logger.warn("Attempted to update non-existent trainer with ID: {}", trainer.getId());
        }
    }
}
