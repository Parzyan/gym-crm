package com.company.gym.service;

import com.company.gym.dao.BaseDAO;
import com.company.gym.entity.Training;
import com.company.gym.entity.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TrainingService extends AbstractBaseService<Training> {
    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);

    private BaseDAO<Training> trainingDAO;

    @Autowired
    public void setTrainingDAO(BaseDAO<Training> trainingDAO) {
        this.trainingDAO = trainingDAO;
        super.setDao(trainingDAO);
    }

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate, Integer duration) {
        if(duration < 0) throw new IllegalArgumentException();

        Training training = new Training();
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        training.setTrainingName(trainingName);
        training.setTrainingType(trainingType);
        training.setTrainingDate(trainingDate);
        training.setDuration(duration);

        trainingDAO.save(training);
        logger.info("Training created: {}, {}", training.getTraineeId(), training.getTrainerId());
        return training;
    }
}
