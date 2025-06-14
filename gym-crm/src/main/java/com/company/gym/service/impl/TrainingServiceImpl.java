package com.company.gym.service.impl;

import com.company.gym.dao.*;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.Training;
import com.company.gym.entity.TrainingType;
import com.company.gym.service.AbstractBaseService;
import com.company.gym.service.TrainingService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TrainingServiceImpl extends AbstractBaseService<Training> implements TrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private TrainingDAO trainingDAO;
    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;
    private TrainingTypeDAO trainingTypeDAO;

    @Autowired
    public void setTrainingDAO(TrainingDAO trainingDAO) {
        this.trainingDAO = trainingDAO;
        super.setDao(trainingDAO);
    }

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setTrainingTypeDAO(TrainingTypeDAO trainingTypeDAO) {
        this.trainingTypeDAO = trainingTypeDAO;
    }

    @Override
    public Training createTraining(String traineeUsername, String trainerUsername, String trainingName,
                                   Long trainingTypeId, Date trainingDate, Integer duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }

        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(traineeUsername);
        if (traineeOpt.isEmpty()) {
            throw new IllegalArgumentException("Trainee not found");
        }

        Optional<Trainer> trainerOpt = trainerDAO.findByUsername(trainerUsername);
        if (trainerOpt.isEmpty()) {
            throw new IllegalArgumentException("Trainer not found");
        }

        Optional<TrainingType> trainingTypeOpt = trainingTypeDAO.findById(trainingTypeId);
        if (trainingTypeOpt.isEmpty()) {
            throw new IllegalArgumentException("Training type not found");
        }

        Training training = new Training();
        training.setTrainee(traineeOpt.get());
        training.setTrainer(trainerOpt.get());
        training.setTrainingName(trainingName);
        training.setTrainingType(trainingTypeOpt.get());
        training.setTrainingDate(trainingDate);
        training.setDuration(duration);

        trainingDAO.save(training);
        logger.info("Created training with ID: {}", training.getId());
        return training;
    }

    @Override
    public List<Training> getTraineeTrainings(String username, Date fromDate, Date toDate,
                                              String trainerName, Long trainingTypeId) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isEmpty()) {
            throw new IllegalArgumentException("Trainee not found");
        }
        return trainingDAO.findTrainingsByTraineeAndCriteria(
                traineeOpt.get().getId(), fromDate, toDate, trainerName, trainingTypeId);
    }

    @Override
    public List<Training> getTrainerTrainings(String username, Date fromDate,
                                              Date toDate, String traineeName) {
        Optional<Trainer> trainerOpt = trainerDAO.findByUsername(username);
        if (trainerOpt.isEmpty()) {
            throw new IllegalArgumentException("Trainer not found");
        }
        return trainingDAO.findTrainingsByTrainerAndCriteria(
                trainerOpt.get().getId(), fromDate, toDate, traineeName);
    }
}
