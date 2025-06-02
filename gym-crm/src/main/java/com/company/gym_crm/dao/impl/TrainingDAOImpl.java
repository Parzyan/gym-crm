package com.company.gym_crm.dao.impl;

import com.company.gym_crm.dao.TrainingDAO;
import com.company.gym_crm.entity.Trainer;
import com.company.gym_crm.entity.Training;
import com.company.gym_crm.service.TraineeService;
import com.company.gym_crm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TrainingDAOImpl implements TrainingDAO {
    private static final Logger logger = LoggerFactory.getLogger(TrainingDAOImpl.class);

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
