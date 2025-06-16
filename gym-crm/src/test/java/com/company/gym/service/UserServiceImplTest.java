package com.company.gym.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.company.gym.dao.impl.UserDAOImpl;
import com.company.gym.entity.User;
import com.company.gym.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDAOImpl userDAO;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void usernameExists_True() {
        when(userDAO.findByUsername("existing.user")).thenReturn(Optional.of(new User()));
        assertTrue(userService.usernameExists("existing.user"));
    }

    @Test
    void usernameExists_False() {
        when(userDAO.findByUsername("new.user")).thenReturn(Optional.empty());
        assertFalse(userService.usernameExists("new.user"));
    }

    @Test
    void usernameExists_Exception() {
        when(userDAO.findByUsername("error.user")).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class,
                () -> userService.usernameExists("error.user"));
    }
}
