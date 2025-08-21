package com.company.gym.service.impl;

import com.company.gym.client.WorkloadServiceClient;
import com.company.gym.dao.*;
import com.company.gym.dto.request.TrainerWorkloadRequest;
import com.company.gym.entity.*;
import com.company.gym.exception.EntityNotFoundException;
import com.company.gym.service.AbstractBaseService;
import com.company.gym.service.TrainingService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
    private WorkloadServiceClient workloadServiceClient;

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

    @Autowired
    public void setWorkloadServiceClient(WorkloadServiceClient workloadServiceClient) {
        this.workloadServiceClient = workloadServiceClient;
    }

    @Override
    public Training createTraining(Credentials traineeCreds, Credentials trainerCreds, String trainingName,
                                   Long trainingTypeId, Date trainingDate, Integer duration) {

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

        TrainerWorkloadRequest payload = new TrainerWorkloadRequest();
        payload.setTrainerUsername(trainer.getUser().getUsername());
        payload.setTrainerFirstName(trainer.getUser().getFirstName());
        payload.setTrainerLastName(trainer.getUser().getLastName());
        payload.setActive(trainer.getUser().getIsActive());
        payload.setTrainingDate(trainingDate);
        payload.setTrainingDuration(duration);
        payload.setActionType(ActionType.ADD);
        workloadServiceClient.updateWorkload(payload);

        logger.info("Created training with ID: {}", training.getId());
        return training;
    }

    @Override
    public void cancelTraining(String traineeUsername, Long trainingId) {
        Training training = trainingDAO.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Training not found with ID: " + trainingId));

        if (!training.getTrainee().getUser().getUsername().equals(traineeUsername)) {
            throw new AccessDeniedException("You are not authorized to cancel this training.");
        }

        if (training.isCanceled()) {
            logger.warn("Training with ID {} is already canceled. No action taken.", trainingId);
            return;
        }

        training.setCanceled(true);
        trainingDAO.update(training);
        logger.info("Training with ID {} has been successfully canceled by user {}.", trainingId, traineeUsername);

        Trainer trainer = training.getTrainer();
        TrainerWorkloadRequest payload = new TrainerWorkloadRequest();
        payload.setTrainerUsername(trainer.getUser().getUsername());
        payload.setTrainerFirstName(trainer.getUser().getFirstName());
        payload.setTrainerLastName(trainer.getUser().getLastName());
        payload.setActive(trainer.getUser().getIsActive());
        payload.setTrainingDate(training.getTrainingDate());
        payload.setTrainingDuration(training.getDuration());
        payload.setActionType(ActionType.DELETE); // Or REMOVE, as you prefer

        workloadServiceClient.updateWorkload(payload);
    }

    @Override
    public List<Training> getTraineeTrainings(Credentials credentials, Date fromDate, Date toDate,
                                              String trainerUsername, Long trainingTypeId) {

        Trainee trainee = traineeDAO.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        return trainingDAO.findTrainingsByTraineeAndCriteria(
                trainee.getId(), fromDate, toDate, trainerUsername, trainingTypeId);
    }

    @Override
    public List<Training> getTrainerTrainings(Credentials credentials, Date fromDate, Date toDate,
                                              String traineeUsername) {

        Trainer trainer = trainerDAO.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        return trainingDAO.findTrainingsByTrainerAndCriteria(
                trainer.getId(), fromDate, toDate, traineeUsername);
    }
}
