package com.company.gym.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.company.gym.dao.impl.UserDAOImpl;
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

@ExtendWith(MockitoExtension.class)
class UserDAOImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<User> query;

    @InjectMocks
    private UserDAOImpl userDAO;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test.user");
    }

    @Test
    void findByUsername_UserExists() {
        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(testUser));

        Optional<User> result = userDAO.findByUsername("test.user");

        assertTrue(result.isPresent());
        assertEquals("test.user", result.get().getUsername());
        verify(query).setParameter("username", "test.user");
    }

    @Test
    void findByUsername_UserNotExists() {
        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        Optional<User> result = userDAO.findByUsername("unknown.user");

        assertFalse(result.isPresent());
    }

    @Test
    void findByUsername_ExceptionHandling() {
        when(entityManager.createQuery(anyString(), eq(User.class))).thenThrow(new RuntimeException("DB error"));

        Optional<User> result = userDAO.findByUsername("test.user");

        assertFalse(result.isPresent());
    }

    @Test
    void save_Success() {
        userDAO.save(testUser);

        verify(entityManager).persist(testUser);
    }

    @Test
    void save_ExceptionHandling() {
        doThrow(new RuntimeException("DB error")).when(entityManager).persist(any(User.class));

        assertThrows(DAOException.class, () -> userDAO.save(testUser));
    }

    @Test
    void update_Success() {
        userDAO.update(testUser);

        verify(entityManager).merge(testUser);
    }

    @Test
    void update_ExceptionHandling() {
        doThrow(new RuntimeException("DB error")).when(entityManager).merge(any(User.class));

        assertThrows(DAOException.class, () -> userDAO.update(testUser));
    }
}
