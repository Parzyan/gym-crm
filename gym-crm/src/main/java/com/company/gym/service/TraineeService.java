package com.company.gym.service;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.entity.Trainee;
import com.company.gym.util.PasswordGenerator;
import com.company.gym.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class TraineeService extends AbstractFullCrudService<Trainee> {
    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);

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

    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();

        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setUsername(username);
        trainee.setPassword(password);
        trainee.setIsActive(true);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        traineeDAO.save(trainee);
        logger.info("Trainee created: {}", trainee.getUsername());
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
}
