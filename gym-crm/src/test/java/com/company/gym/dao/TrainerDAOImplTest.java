package com.company.gym.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.company.gym.dao.impl.TrainerDAOImpl;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.TrainingType;
import com.company.gym.entity.User;
import com.company.gym.exception.DAOException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class TrainerDAOImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Trainer> query;

    @InjectMocks
    private TrainerDAOImpl trainerDAO;

    private Trainer testTrainer;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test.trainer");
        testUser.setPassword("password");
        testUser.setIsActive(true);

        TrainingType testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("Fitness");

        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setUser(testUser);
        testTrainer.setSpecialization(testTrainingType);
    }

    @Test
    void findByUsername_TrainerExists() {
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(testTrainer));

        Optional<Trainer> result = trainerDAO.findByUsername("test.trainer");

        assertTrue(result.isPresent());
        assertEquals("test.trainer", result.get().getUser().getUsername());
        verify(query).setParameter("username", "test.trainer");
    }

    @Test
    void findByUsername_TrainerNotExists() {
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        Optional<Trainer> result = trainerDAO.findByUsername("unknown.trainer");

        assertFalse(result.isPresent());
    }

    @Test
    void findByUsername_ExceptionHandling() {
        when(entityManager.createQuery(anyString(), eq(Trainer.class)))
                .thenThrow(new RuntimeException("DB error"));

        Optional<Trainer> result = trainerDAO.findByUsername("test.trainer");

        assertFalse(result.isPresent());
    }

    @Test
    void save_Success() {
        trainerDAO.save(testTrainer);

        verify(entityManager).persist(testTrainer);
    }

    @Test
    void save_ExceptionHandling() {
        doThrow(new RuntimeException("DB error")).when(entityManager).persist(any(Trainer.class));

        assertThrows(DAOException.class, () -> trainerDAO.save(testTrainer));
    }

    @Test
    void update_Success() {
        trainerDAO.update(testTrainer);

        verify(entityManager).merge(testTrainer);
    }

    @Test
    void update_ExceptionHandling() {
        doThrow(new RuntimeException("DB error")).when(entityManager).merge(any(Trainer.class));

        assertThrows(DAOException.class, () -> trainerDAO.update(testTrainer));
    }

    @Test
    void findBySpecialization_Success() {
        List<Trainer> expected = Collections.singletonList(testTrainer);
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(expected);

        List<Trainer> result = trainerDAO.findBySpecialization(1L);

        assertEquals(1, result.size());
        assertEquals(testTrainer, result.getFirst());
        verify(query).setParameter("trainingTypeId", 1L);
    }

    @Test
    void findBySpecialization_ExceptionHandling() {
        when(entityManager.createQuery(anyString(), eq(Trainer.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(DAOException.class, () -> trainerDAO.findBySpecialization(1L));
    }

    @Test
    void findTrainersNotAssignedToTrainee_Success() {
        List<Trainer> expected = Collections.singletonList(testTrainer);
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(expected);

        List<Trainer> result = trainerDAO.findTrainersNotAssignedToTrainee(1L);

        assertEquals(1, result.size());
        assertEquals(testTrainer, result.getFirst());
        verify(query).setParameter("traineeId", 1L);
    }
}
