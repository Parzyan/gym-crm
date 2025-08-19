package com.company.gym.health;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Health health() {
        try {
            Query query = entityManager.createNativeQuery("SELECT 1");
            query.getSingleResult();
            return Health.up().withDetail("message", "Database connection is healthy").build();
        } catch (Exception e) {
            logger.error("Database health check failed", e);
            return Health.down()
                    .withDetail("error", "Database connection check failed")
                    .build();
        }
    }
}
