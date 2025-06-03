package com.company.gym;

import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.Training;
import com.company.gym.entity.TrainingType;
import com.company.gym.facade.GymFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.util.Optional;

@SpringBootApplication
public class GymCrmApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

	@Autowired
	private GymFacade gymFacade;

	public static void main(String[] args) {
		SpringApplication.run(GymCrmApplication.class, args);
	}

	@Override
	public void run(String... args) {
		Trainee trainee = gymFacade.createTrainee("Bob", "Johnson", LocalDate.of(1990, 1, 1), "123 Street");
		Trainee trainee1 = gymFacade.createTrainee("Mike", "Johnson", LocalDate.of(1995, 1, 1), "124 Street");

		TrainingType specialization = TrainingType.Yoga;
		Trainer trainer = gymFacade.createTrainer("Mike", "Johnson", specialization);

		Optional<Trainee> a = gymFacade.getTrainee(2L);
		Optional<Trainer> b = gymFacade.getTrainer(2L);

		if (a.isPresent()) {
			logger.info("Trainee with the id of 2: {}", a.get().getUsername());
		} else {
			logger.warn("Trainee with the id of 2 not found");
		}

		if (b.isPresent()) {
			logger.info("Trainer with the id of 2: {}", b.get().getUsername());
		} else {
			logger.warn("Trainer with the id of 2 not found");
		}

		Training training = gymFacade.createTraining(2L, 2L, "Introduction", specialization, LocalDate.now(), 25);

		gymFacade.updateTrainee(1L, false, "123 Street");

		gymFacade.deleteTrainee(2L);

		logger.info("All trainees: {}", gymFacade.getAllTrainees().toString());
		logger.info("All trainers: {}", gymFacade.getAllTrainers().toString());
		logger.info("All trainings: {}", gymFacade.getAllTrainings().toString());
	}
}
