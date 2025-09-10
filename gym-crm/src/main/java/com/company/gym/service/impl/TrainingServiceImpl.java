package com.company.gym.service.impl;

import com.company.gym.dao.*;
import com.company.gym.dto.request.TrainerWorkloadRequest;
import com.company.gym.entity.*;
import com.company.gym.exception.EntityNotFoundException;
import com.company.gym.service.AbstractBaseService;
import com.company.gym.service.TrainingService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TrainingServiceImpl extends AbstractBaseService<Training> implements TrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

    @Value("${queue.trainer.workload}")
    private String trainerWorkloadQueue;

    private TrainingDAO trainingDAO;
    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;
    private TrainingTypeDAO trainingTypeDAO;
    private JmsTemplate jmsTemplate;

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
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public Training createTraining(Credentials traineeCreds, Credentials trainerCreds, String trainingName,
                                   Long trainingTypeId, LocalDate trainingDate, Integer duration) {

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
        jmsTemplate.convertAndSend(trainerWorkloadQueue, payload, message -> {
            String transactionId = MDC.get("transactionId");
            if (transactionId != null) {
                message.setStringProperty("X-Transaction-ID", transactionId);
            }
            return message;
        });
        logger.info("Set workload update message for trainer {}", trainer.getUser().getUsername());

        logger.info("Created training with ID: {}", training.getId());
        return training;
    }

    @Override
    public void cancelTraining(String traineeUsername, Long trainingId) {
        Training training = trainingDAO.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Training not found with ID: " + trainingId));

        Trainer trainer = training.getTrainer();
        TrainerWorkloadRequest payload = new TrainerWorkloadRequest();
        payload.setTrainerUsername(trainer.getUser().getUsername());
        payload.setTrainerFirstName(trainer.getUser().getFirstName());
        payload.setTrainerLastName(trainer.getUser().getLastName());
        payload.setActive(trainer.getUser().getIsActive());
        payload.setTrainingDate(training.getTrainingDate());
        payload.setTrainingDuration(training.getDuration());
        payload.setActionType(ActionType.DELETE);

        jmsTemplate.convertAndSend(trainerWorkloadQueue, payload, message -> {
            String transactionId = MDC.get("transactionId");
            if (transactionId != null) {
                message.setStringProperty("X-Transaction-ID", transactionId);
            }
            return message;
        });
        logger.info("Cancel training with ID: {}", training.getId());
    }

    @Override
    public List<Training> getTraineeTrainings(Credentials credentials, LocalDate fromDate, LocalDate toDate,
                                              String trainerUsername, Long trainingTypeId) {

        Trainee trainee = traineeDAO.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        return trainingDAO.findTrainingsByTraineeAndCriteria(
                trainee.getId(), fromDate, toDate, trainerUsername, trainingTypeId);
    }

    @Override
    public List<Training> getTrainerTrainings(Credentials credentials, LocalDate fromDate, LocalDate toDate,
                                              String traineeUsername) {

        Trainer trainer = trainerDAO.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        return trainingDAO.findTrainingsByTrainerAndCriteria(
                trainer.getId(), fromDate, toDate, traineeUsername);
    }
}
