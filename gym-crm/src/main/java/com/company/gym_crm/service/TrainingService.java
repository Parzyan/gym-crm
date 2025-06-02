package com.company.gym_crm.service;

import com.company.gym_crm.dao.TrainingDAO;
import com.company.gym_crm.entity.Trainee;
import com.company.gym_crm.entity.Training;
import com.company.gym_crm.entity.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDAO trainingDAO;

    @Autowired
    public TrainingService(TrainingDAO trainingDAO) {
        this.trainingDAO = trainingDAO;
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

    public Training getTraining(Long id) {
        Optional<Training> optionalTraining = trainingDAO.findById(id);
        if (optionalTraining.isPresent()) {
            return optionalTraining.get();
        }
        return null;
    }

    public List<Training> getAllTrainings() {
        return trainingDAO.findAll();
    }
}
