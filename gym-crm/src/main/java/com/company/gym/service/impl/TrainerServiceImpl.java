package com.company.gym.service.impl;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.dao.TrainingTypeDAO;
import com.company.gym.entity.*;
import com.company.gym.service.AbstractUserService;
import com.company.gym.service.TrainerService;
import com.company.gym.util.PasswordGenerator;
import com.company.gym.util.UsernameGenerator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TrainerServiceImpl extends AbstractUserService<Trainer> implements TrainerService {
    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private TrainerDAO trainerDAO;
    private TraineeDAO traineeDAO;
    private TrainingTypeDAO trainingTypeDAO;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
        super.setDao(trainerDAO);
    }

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setTrainingTypeDAO(TrainingTypeDAO trainingTypeDAO) {
        this.trainingTypeDAO = trainingTypeDAO;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public Trainer createTrainerProfile(String firstName, String lastName, Long specializationId) {
        Optional<TrainingType> trainingTypeOpt = trainingTypeDAO.findById(specializationId);
        if(firstName == null || lastName == null) {
            throw new IllegalArgumentException("FirstName and LastName must not be null");
        }
        if (trainingTypeOpt.isEmpty()) {
            logger.warn("Training type not found for ID: {}", specializationId);
            throw new IllegalArgumentException("Invalid training type");
        }
        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(password);
        user.setIsActive(true);

        Trainer trainer = new Trainer();
        trainer.setSpecialization(trainingTypeOpt.get());
        trainer.setUser(user);

        trainerDAO.save(trainer);
        logger.info("Created trainer profile with username: {}", username);
        return trainer;
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        Optional<Trainer> trainerOpt = trainerDAO.findByUsername(username);
        if (trainerOpt.isPresent()) {
            Trainer trainer = trainerOpt.get();
            if (trainer.getUser().getPassword().equals(oldPassword)) {
                trainer.getUser().setPassword(newPassword);
                trainerDAO.update(trainer);
                logger.info("Password changed for trainer: {}", username);
            } else {
                logger.warn("Password change failed - incorrect old password for trainer: {}", username);
                throw new SecurityException("Incorrect old password");
            }
        } else {
            logger.warn("Password change failed - trainer not found: {}", username);
            throw new IllegalArgumentException("Trainer not found");
        }
    }

    @Override
    public Trainer updateTrainerProfile(Credentials credentials, Long specializationId) {
        validateCredentials(credentials);
        String username = credentials.getUsername();

        Trainer trainer = trainerDAO.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        TrainingType trainingType = trainingTypeDAO.findById(specializationId)
                .orElseThrow(() -> new IllegalArgumentException("Training type not found"));

        trainer.setSpecialization(trainingType);
        trainerDAO.update(trainer);

        logger.info("Updated trainer profile: {}", username);
        return trainer;
    }

    @Override
    public void updateStatus(Credentials credentials) {
        validateCredentials(credentials);
        String username = credentials.getUsername();

        Trainer trainer = trainerDAO.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        boolean newStatus = !trainer.getUser().getIsActive();
        trainer.getUser().setIsActive(newStatus);
        trainerDAO.update(trainer);
        logger.info("Updated trainer {} status to: {}", username, newStatus);
    }

    @Override
    public List<Trainer> getTrainersBySpecialization(Long trainingTypeId) {
        return trainerDAO.findBySpecialization(trainingTypeId);
    }

    @Override
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(traineeUsername);
        if (traineeOpt.isPresent()) {
            return trainerDAO.findTrainersNotAssignedToTrainee(traineeOpt.get().getId());
        }
        throw new IllegalArgumentException("Trainee not found");
    }
}
