package com.company.gym.health;

import com.company.gym.dao.TrainerDAO;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TrainerDaoHealthIndicator implements HealthIndicator {

    private final TrainerDAO trainerDAO;

    public TrainerDaoHealthIndicator(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();

        try {
            long trainerCount = trainerDAO.findAll().size();
            details.put("totalTrainers", trainerCount);

            try {
                int specializationCount = trainerDAO.findBySpecialization(1L).size();
                details.put("specializationQuery", "successful (" + specializationCount + " results)");
            } catch (Exception e) {
                details.put("specializationQuery", "failed: " + e.getMessage());
            }

            try {
                int unassignedCount = trainerDAO.findTrainersNotAssignedToTrainee(1L).size();
                details.put("unassignedTrainersQuery", "successful (" + unassignedCount + " results)");
            } catch (Exception e) {
                details.put("unassignedTrainersQuery", "failed: " + e.getMessage());
            }

            return Health.up()
                    .withDetails(details)
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetails(details)
                    .withDetail("error", "Trainer DAO failure: " + e.getMessage())
                    .build();
        }
    }
}
