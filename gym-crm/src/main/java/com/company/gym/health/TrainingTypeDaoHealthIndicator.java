package com.company.gym.health;

import com.company.gym.dao.TrainingTypeDAO;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TrainingTypeDaoHealthIndicator implements HealthIndicator {

    private final TrainingTypeDAO trainingTypeDAO;

    public TrainingTypeDaoHealthIndicator(TrainingTypeDAO trainingTypeDAO) {
        this.trainingTypeDAO = trainingTypeDAO;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();

        try {
            long typeCount = trainingTypeDAO.findAll().size();
            details.put("trainingTypesCount", typeCount);

            boolean idLookupWorks = trainingTypeDAO.findById(1L).isPresent();
            details.put("idLookup", idLookupWorks ? "successful" : "no type with ID 1 found");

            return Health.up()
                    .withDetails(details)
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetails(details)
                    .withDetail("error", "TrainingType DAO failure: " + e.getMessage())
                    .build();
        }
    }
}
