package com.company.gym.service;

import com.company.gym.dao.TrainerDAO;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.TrainingType;
import com.company.gym.util.PasswordGenerator;
import com.company.gym.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrainerService extends AbstractBaseService<Trainer> {
    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);

    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TrainerDAO trainerDAO;

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
        super.setDao(trainerDAO);
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Trainer createTrainer(String firstName, String lastName, TrainingType specialization) {
        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();

        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setIsActive(true);
        trainer.setSpecialization(specialization);

        trainerDAO.save(trainer);
        logger.info("Trainer created: {}", trainer.getUsername());
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
}
