package com.company.gym.service.impl;

import com.company.gym.dao.TrainingDAO;
import com.company.gym.dao.impl.TraineeDAOImpl;
import com.company.gym.dao.impl.TrainerDAOImpl;
import com.company.gym.dao.impl.UserDAOImpl;
import com.company.gym.dto.request.UpdateTraineeTrainersRequest;
import com.company.gym.dto.response.UserCredentialsResponse;
import com.company.gym.entity.*;
import com.company.gym.exception.EntityNotFoundException;
import com.company.gym.service.AbstractUserService;
import com.company.gym.service.TraineeService;
import com.company.gym.util.PasswordGenerator;
import com.company.gym.util.UsernameGenerator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TraineeServiceImpl extends AbstractUserService<Trainee> implements TraineeService {
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TraineeDAOImpl traineeDAO;
    private TrainerDAOImpl trainerDAO;
    private TrainingDAO trainingDAO;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setTraineeDAO(TraineeDAOImpl traineeDAO) {
        this.traineeDAO = traineeDAO;
        super.setDao(traineeDAO);
    }

    @Autowired
    public void setTrainerDAO(TrainerDAOImpl trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setTrainingDAO(TrainingDAO trainingDAO) {
        this.trainingDAO = trainingDAO;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserCredentialsResponse createTraineeProfile(String firstName, String lastName, Date dateOfBirth, String address) {
        if(firstName == null || lastName == null) {
            throw new IllegalArgumentException("FirstName and LastName must not be null");
        }

        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setIsActive(true);

        Trainee trainee = new Trainee();
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.setUser(user);

        traineeDAO.save(trainee);
        logger.info("Created trainee profile with username: {}", username);
        return new UserCredentialsResponse(username, password);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            validateCredentials(new Credentials(username, oldPassword));
            trainee.getUser().setPassword(passwordEncoder.encode(newPassword));
            traineeDAO.update(trainee);
            logger.info("Password changed for trainee: {}", username);
        } else {
            logger.warn("Password change failed - trainee not found: {}", username);
            throw new SecurityException("Trainee not found");
        }
    }

    @Override
    public Trainee updateTraineeProfile(Credentials credentials, Date dateOfBirth, String address) {
        String username = credentials.getUsername();
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            if (dateOfBirth != null) {
                trainee.setDateOfBirth(dateOfBirth);
            }
            if (address != null) {
                trainee.setAddress(address);
            }
            traineeDAO.update(trainee);
            logger.info("Updated trainee profile: {}", username);
            return trainee;
        }
        logger.warn("Trainee not found for update: {}", username);
        throw new IllegalArgumentException("Trainee not found for update");
    }

    @Override
    public void updateStatus(Credentials credentials) {
        validateCredentials(credentials);
        String username = credentials.getUsername();
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            if (trainee.getUser().getIsActive()) {
                trainee.getUser().setIsActive(false);
                traineeDAO.update(trainee);
                logger.info("Updated trainee {} status to: {}", username, false);
            } else {
                trainee.getUser().setIsActive(true);
                traineeDAO.update(trainee);
                logger.info("Trainee {} status already set to: {}", username, true);
            }
        } else {
            logger.warn("Trainee not found for status update: {}", username);
            throw new IllegalArgumentException("Trainee not found");
        }
    }

    @Override
    @Transactional
    public void deleteTraineeProfile(Credentials credentials) {
        String username = credentials.getUsername();
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            traineeDAO.delete(traineeOpt.get().getId());
            logger.info("Deleted trainee profile: {}", username);
        } else {
            logger.warn("Trainee not found for deletion: {}", username);
            throw new IllegalArgumentException("Trainee not found");
        }
    }

    @Override
    public void updateTraineeTrainers(Credentials credentials, Set<Long> trainerIds) {
        String traineeUsername = credentials.getUsername();
        Trainee trainee = traineeDAO.findByUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        Set<Trainer> trainers = trainerIds.stream()
                .map(id -> trainerDAO.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + id)))
                .collect(Collectors.toSet());

        trainee.setTrainers(trainers);
        traineeDAO.update(trainee);
    }

    @Override
    public List<Trainer> getTrainersForTrainee(String traineeUsername) {
        Trainee trainee = traineeDAO.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + traineeUsername));

        return trainee.getTrainings().stream()
                .map(Training::getTrainer)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Trainer> updateTrainingTrainers(Credentials credentials,
                                                List<UpdateTraineeTrainersRequest.TrainingTrainerUpdate> updates) {
        Trainee authenticatedTrainee = traineeDAO.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainee not found with username: " + credentials.getUsername()));

        for (UpdateTraineeTrainersRequest.TrainingTrainerUpdate update : updates) {
            Training training = trainingDAO.findById(update.getTrainingId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Training not found with ID: " + update.getTrainingId()));

            if (!training.getTrainee().getId().equals(authenticatedTrainee.getId())) {
                throw new SecurityException("Trainee " + credentials.getUsername() +
                        " doesn't own training with ID: " + update.getTrainingId());
            }

            Trainer newTrainer = trainerDAO.findByUsername(update.getTrainerUsername())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Trainer not found with username: " + update.getTrainerUsername()));

            training.setTrainer(newTrainer);
            trainingDAO.update(training);
        }

        List<Training> updatedTrainings = trainingDAO.findTrainingsByTraineeAndCriteria(
                authenticatedTrainee.getId(), null, null, null, null);

        return updatedTrainings.stream()
                .map(Training::getTrainer)
                .distinct()
                .collect(Collectors.toList());
    }
}
