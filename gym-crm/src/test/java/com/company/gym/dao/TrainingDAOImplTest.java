package com.company.gym.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import com.company.gym.dao.impl.TrainingDAOImpl;
import com.company.gym.entity.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainingDAOImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Training> query;

    @InjectMocks
    private TrainingDAOImpl trainingDAO;

    @Test
    void save_Success() {
        Training training = new Training();
        training.setId(1L);

        trainingDAO.save(training);

        verify(entityManager).persist(training);
    }

    @Test
    void findById_Exists() {
        Training expected = new Training();
        when(entityManager.find(Training.class, 1L)).thenReturn(expected);

        Optional<Training> result = trainingDAO.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void findById_NotExists() {
        when(entityManager.find(Training.class, 1L)).thenReturn(null);

        Optional<Training> result = trainingDAO.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll() {
        List<Training> expected = Arrays.asList(new Training(), new Training());
        when(entityManager.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(expected);

        List<Training> result = trainingDAO.findAll();

        assertEquals(2, result.size());
        verify(entityManager).createQuery("FROM Training", Training.class);
    }

    @Test
    void findTrainingsByTraineeAndCriteria_AllParams() {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();
        String trainerUsername = "john.doe";
        Long trainingTypeId = 1L;
        List<Training> expected = List.of(new Training());

        when(entityManager.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(expected);

        List<Training> result = trainingDAO.findTrainingsByTraineeAndCriteria(
                1L, fromDate, toDate, trainerUsername, trainingTypeId);

        assertEquals(1, result.size());
        verify(query).setParameter("traineeId", 1L);
        verify(query).setParameter("fromDate", fromDate);
        verify(query).setParameter("toDate", toDate);
        verify(query).setParameter("trainerUsername", "john.doe");
        verify(query).setParameter("trainingTypeId", trainingTypeId);
    }

    @Test
    void findTrainingsByTraineeAndCriteria_NullParams() {
        List<Training> expected = List.of(new Training());

        when(entityManager.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(expected);

        List<Training> result = trainingDAO.findTrainingsByTraineeAndCriteria(
                1L, null, null, null, null);

        assertEquals(1, result.size());
        verify(query).setParameter("traineeId", 1L);
        verify(query, never()).setParameter(eq("fromDate"), any());
        verify(query, never()).setParameter(eq("toDate"), any());
        verify(query, never()).setParameter(eq("trainerName"), any());
        verify(query, never()).setParameter(eq("trainingTypeId"), any());
    }

    @Test
    void findTrainingsByTrainerAndCriteria_AllParams() {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();
        String traineeUsername = "jane.smith";
        List<Training> expected = List.of(new Training());

        when(entityManager.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(expected);

        List<Training> result = trainingDAO.findTrainingsByTrainerAndCriteria(
                1L, fromDate, toDate, traineeUsername);

        assertEquals(1, result.size());
        verify(query).setParameter("trainerId", 1L);
        verify(query).setParameter("fromDate", fromDate);
        verify(query).setParameter("toDate", toDate);
        verify(query).setParameter("traineeUsername", "jane.smith");
    }

    @Test
    void findTrainingsByTrainerAndCriteria_NullParams() {
        List<Training> expected = List.of(new Training());

        when(entityManager.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(expected);

        List<Training> result = trainingDAO.findTrainingsByTrainerAndCriteria(
                1L, null, null, null);

        assertEquals(1, result.size());
        verify(query).setParameter("trainerId", 1L);
        verify(query, never()).setParameter(eq("fromDate"), any());
        verify(query, never()).setParameter(eq("toDate"), any());
        verify(query, never()).setParameter(eq("traineeName"), any());
    }
}
