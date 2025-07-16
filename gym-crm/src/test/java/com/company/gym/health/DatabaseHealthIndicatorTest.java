package com.company.gym.health;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private DatabaseHealthIndicator databaseHealthIndicator;

    @BeforeEach
    void setUp() {
        when(entityManager.createNativeQuery("SELECT 1")).thenReturn(query);
    }

    @Test
    void whenDatabaseIsUp() {
        when(query.getSingleResult()).thenReturn(1);

        Health health = databaseHealthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("Database connection is healthy", health.getDetails().get("message"));
    }

    @Test
    void whenDatabaseIsDown() {
        when(query.getSingleResult()).thenThrow(new PersistenceException("Connection failed"));

        Health health = databaseHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Database connection check failed", health.getDetails().get("error"));
    }
}
