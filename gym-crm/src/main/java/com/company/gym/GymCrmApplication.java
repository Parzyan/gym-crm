package com.company.gym;

import com.company.gym.facade.GymFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.Calendar;
import java.util.Date;

@SpringBootApplication
@EntityScan(basePackages = "com.company.gym.entity")
@EnableFeignClients
public class GymCrmApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

	@Autowired
	private GymFacade gymFacade;

	public static void main(String[] args) {
		SpringApplication.run(GymCrmApplication.class, args);
	}

	@Override
	public void run(String... args) {
		logger.info("Created trainee");
		gymFacade.createTrainee("Bob", "Johnson", new Date(90, Calendar.JANUARY, 1), "123 Street");
		gymFacade.createTrainee("Bob", "Johnson", new Date(91, Calendar.JANUARY, 1), "123 Street");
		gymFacade.createTrainee("Bob", "Johnson", new Date(92, Calendar.JANUARY, 1), "123 Street");
		gymFacade.createTrainee("Mike", "Johnson", new Date(95, Calendar.JANUARY, 1), "124 Street");

		gymFacade.createTrainer("Mike", "Johnson", 1L);
		gymFacade.createTrainer("Alex", "Pitt", 2L);
	}
}
