package com.company.gym.dao.impl;

import com.company.gym.dao.TrainingDAO;
import com.company.gym.entity.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TrainingDAOImpl implements TrainingDAO {
    private final Map<Long, Training> storage;
    private long currentId = 1;

    @Autowired
    public TrainingDAOImpl(@Qualifier("trainingStorage") Map<Long, Training> storage) {
        this.storage = storage;
        if (!storage.isEmpty()) {
            currentId = Collections.max(storage.keySet()) + 1;
        }
    }

    @Override
    public void save(Training training) {
        training.setId(currentId++);
        storage.put(training.getId(), training);
    }

    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Training> findAll() {
        return new ArrayList<>(storage.values());
    }
}
