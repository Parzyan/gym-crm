package com.company.gym_crm.service;

import com.company.gym_crm.dao.TraineeDAO;
import com.company.gym_crm.entity.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {
    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);
    private static final int password_length= 10;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    private final TraineeDAO traineeDAO;

    @Autowired
    public TraineeService(TraineeDAO traineeDAO,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator) {
        this.traineeDAO = traineeDAO;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword(password_length);

        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setUsername(username);
        trainee.setPassword(password);
        trainee.setIsActive(true);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        traineeDAO.save(trainee);
        logger.info("Trainee created: {}, {}", trainee.getUsername(), trainee.getPassword());
        return trainee;
    }

    public Trainee updateTrainee(Long id, Boolean isActive, String address) {
        Optional<Trainee> optionalTrainee = traineeDAO.findById(id);
        if (optionalTrainee.isPresent()) {
            Trainee trainee = optionalTrainee.get();
            trainee.setIsActive(isActive);
            trainee.setAddress(address);

            traineeDAO.update(trainee);
            logger.info("Updated trainee with id: {}", id);
            return trainee;
        }
        logger.info("Trainee with id: {} not found", id);
        return null;
    }

    public void deleteTrainee(Long id) {
        traineeDAO.delete(id);
        logger.info("Trainee with id: {} deleted", id);
    }

    public Trainee getTrainee(Long id) {
        Optional<Trainee> optionalTrainee = traineeDAO.findById(id);
        if (optionalTrainee.isPresent()) {
            return optionalTrainee.get();
        }
        return null;
    }

    public List<Trainee> getAllTrainees() {
        return traineeDAO.findAll();
    }
}
