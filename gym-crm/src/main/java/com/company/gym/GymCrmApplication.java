package com.company.gym;

import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.facade.GymFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@SpringBootApplication
@EntityScan(basePackages = "com.company.gym.entity")
public class GymCrmApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

	@Autowired
	private GymFacade gymFacade;

	public static void main(String[] args) {
		SpringApplication.run(GymCrmApplication.class, args);
	}

	@Override
	public void run(String... args) {
		gymFacade.createTrainee("Bob", "Johnson", new Date(90, 0, 1), "123 Street");
		gymFacade.createTrainee("Bob", "Johnson", new Date(91, 0, 1), "123 Street");
		gymFacade.createTrainee("Bob", "Johnson", new Date(92, 0, 1), "123 Street");
		gymFacade.createTrainee("Mike", "Johnson", new Date(95, 0, 1), "124 Street");

		gymFacade.createTrainer("Mike", "Johnson", 1L);

		Optional<Trainee> a = gymFacade.getTrainee(2L);
		Optional<Trainer> b = gymFacade.getTrainer(2L);

		if (a.isPresent()) {
			logger.info("Trainee with the id of 2: {}", a.get().getUser().getUsername());
		} else {
			logger.warn("Trainee with the id of 2 not found");
		}

		if (b.isPresent()) {
			logger.info("Trainer with the id of 2: {}", b.get().getUser().getUsername());
		} else {
			logger.warn("Trainer with the id of 2 not found");
		}

		gymFacade.updateTraineeStatus("password12", "Bob.Johnson2");

		gymFacade.createTraining("Bob.Johnson1", "password12", "Mike.Johnson1", "password12",
				"Introduction", 2L, new Date(), 20);

		gymFacade.deleteTrainee("password12", "Bob.Johnson1");

		logger.info("All Active trainees: {}", Arrays.toString(gymFacade.getActiveTrainees().toArray()));
	}
}
