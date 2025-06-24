package com.company.gym.dao;

import com.company.gym.dao.impl.TraineeDAOImpl;
import com.company.gym.entity.Trainee;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeDAOImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Trainee> query;

    @InjectMocks
    private TraineeDAOImpl traineeDAO;

    private Trainee testTrainee;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test.trainee");
        testUser.setPassword("password");
        testUser.setIsActive(true);

        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUser(testUser);
    }

    @Test
    void findByUsername_TraineeExists() {
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(testTrainee));

        Optional<Trainee> result = traineeDAO.findByUsername("test.trainee");

        assertTrue(result.isPresent());
        assertEquals("test.trainee", result.get().getUser().getUsername());
        verify(query).setParameter("username", "test.trainee");
    }

    @Test
    void findByUsername_TraineeNotExists() {
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        Optional<Trainee> result = traineeDAO.findByUsername("unknown.trainee");

        assertFalse(result.isPresent());
    }

    @Test
    void findByUsername_ExceptionHandling() {
        when(entityManager.createQuery(anyString(), eq(Trainee.class)))
                .thenThrow(new RuntimeException("DB error"));

        Optional<Trainee> result = traineeDAO.findByUsername("test.trainee");

        assertFalse(result.isPresent());
    }

    @Test
    void save_Success() {
        traineeDAO.save(testTrainee);

        verify(entityManager).persist(testTrainee);
    }

    @Test
    void save_ExceptionHandling() {
        doThrow(new RuntimeException("DB error")).when(entityManager).persist(any(Trainee.class));

        assertThrows(DAOException.class, () -> traineeDAO.save(testTrainee));
    }

    @Test
    void update_Success() {
        traineeDAO.update(testTrainee);

        verify(entityManager).merge(testTrainee);}

    @Test
    void update_ExceptionHandling() {
        doThrow(new RuntimeException("DB error")).when(entityManager).merge(any(Trainee.class));

        assertThrows(DAOException.class, () -> traineeDAO.update(testTrainee));
    }

    @Test
    void delete_Success() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(testTrainee);

        traineeDAO.delete(1L);

        verify(entityManager).remove(testTrainee);
    }

    @Test
    void delete_TraineeNotFound() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(null);

        traineeDAO.delete(1L);

        verify(entityManager, never()).remove(any(Trainee.class));
    }

    @Test
    void delete_ExceptionHandling() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(testTrainee);
        doThrow(new RuntimeException("DB error")).when(entityManager).remove(any(Trainee.class));

        assertThrows(DAOException.class, () -> traineeDAO.delete(1L));
    }

    @Test
    void changePassword_Success() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(testTrainee);

        traineeDAO.changePassword(1L, "newPassword");

        assertEquals("newPassword", testTrainee.getUser().getPassword());
        verify(entityManager).merge(testTrainee);
    }

    @Test
    void changePassword_TraineeNotFound() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(null);

        traineeDAO.changePassword(1L, "newPassword");

        verify(entityManager, never()).merge(any(Trainee.class));
    }

    @Test
    void updateStatus_Success() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(testTrainee);
        boolean initialStatus = testTrainee.getUser().getIsActive();

        traineeDAO.updateStatus(1L);

        assertEquals(!initialStatus, testTrainee.getUser().getIsActive());
        verify(entityManager).merge(testTrainee);
    }

    @Test
    void updateStatus_TraineeNotFound() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(null);

        traineeDAO.updateStatus(1L);

        verify(entityManager, never()).merge(any(Trainee.class));
    }

    @Test
    void findById_TraineeExists() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(testTrainee);

        Optional<Trainee> result = traineeDAO.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    void findById_TraineeNotExists() {
        when(entityManager.find(Trainee.class, 1L)).thenReturn(null);

        Optional<Trainee> result = traineeDAO.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll() {
        List<Trainee> expected = Collections.singletonList(testTrainee);
        when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(expected);

        List<Trainee> result = traineeDAO.findAll();

        assertEquals(1, result.size());
        assertEquals(testTrainee, result.getFirst());
        verify(entityManager).createQuery("FROM Trainee", Trainee.class);
    }
}
