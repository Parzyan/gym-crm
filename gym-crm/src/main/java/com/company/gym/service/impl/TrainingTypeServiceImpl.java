package com.company.gym.service.impl;

import com.company.gym.dao.TrainingTypeDAO;
import com.company.gym.entity.TrainingType;
import com.company.gym.service.TrainingTypeService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private TrainingTypeDAO trainingTypeDAO;

    @Autowired
    public void setTrainingTypeDAO(TrainingTypeDAO trainingTypeDAO) {
        this.trainingTypeDAO = trainingTypeDAO;
    }

    public List<TrainingType> getAll() {
        return trainingTypeDAO.findAll();
    }
}
