package com.company.gym.service.impl;

import com.company.gym.dao.*;
import com.company.gym.entity.*;
import com.company.gym.service.AbstractBaseService;
import com.company.gym.service.TrainingService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
    public Training createTraining(Credentials traineeCreds, Credentials trainerCreds, String trainingName,
                                   Long trainingTypeId, Date trainingDate, Integer duration) {
        validateCredentials(traineeCreds);
        validateCredentials(trainerCreds);

        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }

        Trainee trainee = traineeDAO.findByUsername(traineeCreds.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));
        Trainer trainer = trainerDAO.findByUsername(trainerCreds.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
        TrainingType trainingType = trainingTypeDAO.findById(trainingTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Training type not found"));

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(trainingName);
        training.setTrainingType(trainingType);
        training.setTrainingDate(trainingDate);
        training.setDuration(duration);

        trainingDAO.save(training);
        logger.info("Created training with ID: {}", training.getId());
        return training;
    }

    @Override
    public List<Training> getTraineeTrainings(Credentials credentials, Date fromDate, Date toDate,
                                              String trainerUsername, Long trainingTypeId) {
        validateCredentials(credentials);

        Trainee trainee = traineeDAO.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        return trainingDAO.findTrainingsByTraineeAndCriteria(
                trainee.getId(), fromDate, toDate, trainerUsername, trainingTypeId);
    }

    @Override
    public List<Training> getTrainerTrainings(Credentials credentials, Date fromDate, Date toDate,
                                              String traineeUsername) {
        validateCredentials(credentials);

        Trainer trainer = trainerDAO.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        return trainingDAO.findTrainingsByTrainerAndCriteria(
                trainer.getId(), fromDate, toDate, traineeUsername);
    }
}
