package com.company.gym.dao;

import com.company.gym.dao.impl.TrainingTypeDAOImpl;
import com.company.gym.entity.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeDAOImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<TrainingType> query;

    @InjectMocks
    private TrainingTypeDAOImpl trainingTypeDAO;

    private TrainingType testTrainingType;

    @BeforeEach
    void setUp() {
        testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("Yoga");
    }

    @Test
    void findByName_Success() {
        when(entityManager.createQuery(TrainingTypeDAOImpl.FIND_BY_NAME_QUERY, TrainingType.class))
                .thenReturn(query);
        when(query.setParameter("name", "Yoga")).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(testTrainingType));

        Optional<TrainingType> result = trainingTypeDAO.findByName("Yoga");

        assertTrue(result.isPresent());
        assertEquals("Yoga", result.get().getTrainingTypeName());
        verify(entityManager).createQuery(TrainingTypeDAOImpl.FIND_BY_NAME_QUERY, TrainingType.class);
        verify(query).setParameter("name", "Yoga");
        verify(query).getResultList();
    }

    @Test
    void findByName_NotFound() {
        when(entityManager.createQuery(TrainingTypeDAOImpl.FIND_BY_NAME_QUERY, TrainingType.class))
                .thenReturn(query);
        when(query.setParameter("name", "Pilates")).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        Optional<TrainingType> result = trainingTypeDAO.findByName("Pilates");

        assertFalse(result.isPresent());
    }

    @Test
    void findById_Success() {
        when(entityManager.find(TrainingType.class, 1L)).thenReturn(testTrainingType);

        Optional<TrainingType> result = trainingTypeDAO.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(entityManager).find(TrainingType.class, 1L);
    }

    @Test
    void findById_NotFound() {
        when(entityManager.find(TrainingType.class, 99L)).thenReturn(null);

        Optional<TrainingType> result = trainingTypeDAO.findById(99L);

        assertFalse(result.isPresent());
    }
}
