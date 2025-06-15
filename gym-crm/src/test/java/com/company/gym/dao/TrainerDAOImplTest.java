package com.company.gym.dao;

import com.company.gym.dao.impl.TrainerDAOImpl;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.TrainingType;
import com.company.gym.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerDAOImplTest {

    private EntityManager entityManager;
    private TrainerDAOImpl trainerDAO;

    private Trainer testTrainer;
    private User testUser;
    private TrainingType testTrainingType;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        trainerDAO = new TrainerDAOImpl();
        ReflectionTestUtils.setField(trainerDAO, "entityManager", entityManager);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test.trainer");
        testUser.setPassword("password");
        testUser.setIsActive(true);

        testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("Yoga");

        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setUser(testUser);
        testTrainer.setSpecialization(testTrainingType);
    }

    @Test
    void save_Success() {
        doNothing().when(entityManager).persist(any(Trainer.class));

        trainerDAO.save(testTrainer);

        verify(entityManager).persist(testTrainer);
    }

    @Test
    void findById_Success() {
        when(entityManager.find(Trainer.class, 1L)).thenReturn(testTrainer);

        Optional<Trainer> result = trainerDAO.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    void findAll_Success() {
        TypedQuery<Trainer> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(testTrainer));

        List<Trainer> result = trainerDAO.findAll();

        assertEquals(1, result.size());
        assertEquals(testTrainer, result.getFirst());
    }

    @Test
    void update_Success() {
        when(entityManager.merge(testTrainer)).thenReturn(testTrainer);

        trainerDAO.update(testTrainer);

        verify(entityManager).merge(testTrainer);
    }

    @Test
    void findByUsername_Success() {
        TypedQuery<Trainer> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyLong())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(testTrainer));

        Optional<Trainer> result = trainerDAO.findByUsername("test.trainer");

        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    void findBySpecialization_Success() {
        TypedQuery<Trainer> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyLong())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(testTrainer));

        List<Trainer> result = trainerDAO.findBySpecialization(1L);

        assertEquals(1, result.size());
        assertEquals(testTrainer, result.getFirst());
    }

    @Test
    void changePassword_Success() {
        when(entityManager.find(Trainer.class, 1L)).thenReturn(testTrainer);
        when(entityManager.merge(testTrainer)).thenReturn(testTrainer);

        trainerDAO.changePassword(1L, "newPassword");

        assertEquals("newPassword", testTrainer.getUser().getPassword());
        verify(entityManager).merge(testTrainer);
    }

    @Test
    void updateActivity_Success() {
        when(entityManager.find(Trainer.class, 1L)).thenReturn(testTrainer);
        when(entityManager.merge(testTrainer)).thenReturn(testTrainer);

        trainerDAO.updateActivity(1L, false);

        assertFalse(testTrainer.getUser().getIsActive());
        verify(entityManager).merge(testTrainer);
    }

    @Test
    void findTrainersNotAssignedToTrainee_Success() {
        TypedQuery<Trainer> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyLong())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(testTrainer));

        List<Trainer> result = trainerDAO.findTrainersNotAssignedToTrainee(1L);

        assertEquals(1, result.size());
        assertEquals(testTrainer, result.get(0));
    }
}
