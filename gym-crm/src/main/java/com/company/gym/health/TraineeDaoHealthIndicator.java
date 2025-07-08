package com.company.gym.health;

import com.company.gym.dao.TraineeDAO;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TraineeDaoHealthIndicator implements HealthIndicator {

    private final TraineeDAO traineeDAO;

    public TraineeDaoHealthIndicator(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();

        try {
            long traineeCount = traineeDAO.findAll().size();
            details.put("totalTrainees", traineeCount);

            try {
                traineeDAO.findAll().stream().findFirst().ifPresent(trainee -> {
                    trainee.getUser().setFirstName(trainee.getUser().getFirstName());
                    traineeDAO.update(trainee);
                });
                details.put("updateOperation", "successful");
            } catch (Exception e) {
                details.put("updateOperation", "failed: " + e.getMessage());
                throw e;
            }

            return Health.up()
                    .withDetails(details)
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetails(details)
                    .withDetail("error", "Trainee DAO failure: " + e.getMessage())
                    .build();
        }
    }
}
