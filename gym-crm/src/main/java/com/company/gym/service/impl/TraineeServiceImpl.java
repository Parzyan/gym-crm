package com.company.gym.service.impl;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.User;
import com.company.gym.service.AbstractCrudService;
import com.company.gym.service.TraineeService;
import com.company.gym.util.PasswordGenerator;
import com.company.gym.util.UsernameGenerator;
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
public class TraineeServiceImpl extends AbstractCrudService<Trainee> implements TraineeService {
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TraineeDAO traineeDAO;

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
        super.setDao(traineeDAO);
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
    public Trainee createTraineeProfile(String firstName, String lastName, Date dateOfBirth, String address) {
        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(password);
        user.setIsActive(true);

        Trainee trainee = new Trainee();
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.setUser(user);

        traineeDAO.save(trainee);
        logger.info("Created trainee profile with username: {}", username);
        return trainee;
    }

    @Override
    public boolean authenticateTrainee(String username, String password) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        return traineeOpt.isPresent() &&
                traineeOpt.get().getUser().getPassword().equals(password) &&
                traineeOpt.get().getUser().getIsActive();
    }

    @Override
    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            if (trainee.getUser().getPassword().equals(oldPassword)) {
                trainee.getUser().setPassword(newPassword);
                traineeDAO.update(trainee);
                logger.info("Password changed for trainee: {}", username);
            } else {
                logger.warn("Password change failed - incorrect old password for trainee: {}", username);
                throw new SecurityException("Incorrect old password");
            }
        } else {
            logger.warn("Password change failed - trainee not found: {}", username);
            throw new IllegalArgumentException("Trainee not found");
        }
    }

    @Override
    public Trainee updateTraineeProfile(String username, Date dateOfBirth, String address) {
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
        throw new IllegalArgumentException("Trainee not found");
    }

    @Override
    public void updateTraineeStatus(String username, boolean isActive) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            if (trainee.getUser().getIsActive() != isActive) {
                trainee.getUser().setIsActive(isActive);
                traineeDAO.update(trainee);
                logger.info("Updated trainee {} status to: {}", username, isActive);
            } else {
                logger.info("Trainee {} status already set to: {}", username, isActive);
            }
        } else {
            logger.warn("Trainee not found for status update: {}", username);
            throw new IllegalArgumentException("Trainee not found");
        }
    }

    @Override
    public void deleteTraineeProfile(String username) {
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
    public List<Trainee> getActiveTrainees() {
        return traineeDAO.findActiveTrainees();
    }
}
