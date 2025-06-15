package com.company.gym.dao;

import com.company.gym.dao.impl.TraineeDAOImpl;
import com.company.gym.entity.Trainee;
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


class TraineeDAOImplTest {

    private EntityManager entityManager;
    private TraineeDAOImpl traineeDAO;

    private Trainee testTrainee;
    private User testUser;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        traineeDAO = new TraineeDAOImpl();
        ReflectionTestUtils.setField(traineeDAO, "entityManager", entityManager);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test.user");
        testUser.setPassword("password");
        testUser.setIsActive(true);

        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUser(testUser);
    }

    @Test
    void save_Success() {
        doNothing().when(entityManager).persist(any(Trainee.class));

        traineeDAO.save(testTrainee);

        verify(entityManager).persist(testTrainee);
    }

    @Test
    void findById_Success() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(testTrainee);

        Optional<Trainee> result = traineeDAO.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    void findById_NotFound() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(null);

        Optional<Trainee> result = traineeDAO.findById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_Success() {
        TypedQuery<Trainee> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(testTrainee));

        List<Trainee> result = traineeDAO.findAll();

        assertEquals(1, result.size());
        assertEquals(testTrainee, result.getFirst());
    }

    @Test
    void update_Success() {
        when(entityManager.merge(testTrainee)).thenReturn(testTrainee);

        traineeDAO.update(testTrainee);

        verify(entityManager).merge(testTrainee);
    }

    @Test
    void delete_Success() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(testTrainee);
        doNothing().when(entityManager).remove(testTrainee);

        traineeDAO.delete(1L);

        verify(entityManager).remove(testTrainee);
    }

    @Test
    void findByUsername_Success() {
        TypedQuery<Trainee> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyLong())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(testTrainee));

        Optional<Trainee> result = traineeDAO.findByUsername("test.user");

        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    void findActiveTrainees_Success() {
        TypedQuery<Trainee> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(testTrainee));

        List<Trainee> result = traineeDAO.findActiveTrainees();

        assertEquals(1, result.size());
        assertEquals(testTrainee, result.get(0));
    }

    @Test
    void changePassword_Success() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(testTrainee);
        when(entityManager.merge(testTrainee)).thenReturn(testTrainee);

        traineeDAO.changePassword(1L, "newPassword");

        assertEquals("newPassword", testTrainee.getUser().getPassword());
        verify(entityManager).merge(testTrainee);
    }

    @Test
    void updateActivity_Success() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(testTrainee);
        when(entityManager.merge(testTrainee)).thenReturn(testTrainee);

        traineeDAO.updateActivity(1L, false);

        assertFalse(testTrainee.getUser().getIsActive());
        verify(entityManager).merge(testTrainee);
    }
}
