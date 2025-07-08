package com.company.gym.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import com.company.gym.dao.TrainingDAO;
import java.util.HashMap;
import java.util.Map;

@Component
public class TrainingDaoHealthIndicator implements HealthIndicator {

    private final TrainingDAO trainingDAO;

    public TrainingDaoHealthIndicator(TrainingDAO trainingDAO) {
        this.trainingDAO = trainingDAO;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();

        try {
            long totalTrainings = trainingDAO.findAll().size();
            details.put("totalTrainings", totalTrainings);

            int traineeCriteriaResults = trainingDAO
                    .findTrainingsByTraineeAndCriteria(1L, null, null, null, null)
                    .size();
            details.put("traineeCriteriaQuery", "successful (" + traineeCriteriaResults + " results)");

            int trainerCriteriaResults = trainingDAO
                    .findTrainingsByTrainerAndCriteria(1L, null, null, null)
                    .size();
            details.put("trainerCriteriaQuery", "successful (" + trainerCriteriaResults + " results)");

            try {
                trainingDAO.findAll().stream().findFirst().ifPresent(training -> {
                    training.setDuration(training.getDuration());
                    trainingDAO.update(training);
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
                    .withDetail("error", "Training DAO failure: " + e.getMessage())
                    .build();
        }
    }
}
