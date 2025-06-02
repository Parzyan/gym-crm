package com.company.gym_crm.service;

import com.company.gym_crm.dao.TrainerDAO;
import com.company.gym_crm.entity.Trainer;
import com.company.gym_crm.entity.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {
    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);
    private static final int password_length = 10;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    private final TrainerDAO trainerDAO;

    @Autowired
    public TrainerService(TrainerDAO trainerDAO,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator) {
        this.trainerDAO = trainerDAO;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    public Trainer createTrainer(String firstName, String lastName, TrainingType specialization) {
        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword(password_length);

        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setIsActive(true);
        trainer.setSpecialization(specialization);

        trainerDAO.save(trainer);
        logger.info("Trainer created: {}, {}", trainer.getUsername(), trainer.getPassword());
        return trainer;
    }

    public Trainer updateTrainer(Long id, Boolean isActive, TrainingType specialization) {
        Optional<Trainer> optionalTrainer = trainerDAO.findById(id);
        if (optionalTrainer.isPresent()) {
            Trainer trainer = optionalTrainer.get();
            trainer.setIsActive(isActive);
            trainer.setSpecialization(specialization);

            trainerDAO.update(trainer);
            logger.info("Updated trainer with id: {}", id);
            return trainer;
        }
        logger.info("Trainer with id: {} not found", id);
        return null;
    }

    public Trainer getTrainer(Long id) {
        Optional<Trainer> optionalTrainer = trainerDAO.findById(id);
        if (optionalTrainer.isPresent()) {
            return optionalTrainer.get();
        }
        return null;
    }

    public List<Trainer> getAllTrainers() {
        return trainerDAO.findAll();
    }
}
