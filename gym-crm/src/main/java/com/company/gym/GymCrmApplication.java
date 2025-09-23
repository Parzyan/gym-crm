package com.company.gym;

import com.company.gym.facade.GymFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.Calendar;
import java.util.Date;

@SpringBootApplication
@EntityScan(basePackages = "com.company.gym.entity")
public class GymCrmApplication {

	private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(GymCrmApplication.class, args);
	}
}
