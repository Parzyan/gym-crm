package com.company.gym.service;

import com.company.gym.dao.impl.TraineeDAOImpl;
import com.company.gym.dao.impl.TrainerDAOImpl;
import com.company.gym.entity.*;
import com.company.gym.service.impl.AuthenticationServiceImpl;
import com.company.gym.service.impl.TraineeServiceImpl;
import com.company.gym.util.PasswordGenerator;
import com.company.gym.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeDAOImpl traineeDAO;
    @Mock
    private TrainerDAOImpl trainerDAO;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private AuthenticationServiceImpl authenticationService;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee testTrainee;
    private Credentials validCredentials;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test.user");
        testUser.setPassword("oldPassword");
        testUser.setIsActive(true);

        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUser(testUser);
        testTrainee.setDateOfBirth(new Date());
        testTrainee.setAddress("123 Street");

        validCredentials = new Credentials("test.user", "oldPassword");
    }

    @Test
    void createTraineeProfile_Success() {
        when(usernameGenerator.generateUsername("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("generatedPassword");

        Trainee result = traineeService.createTraineeProfile("John", "Smith", new Date(), "123 Main St");

        assertNotNull(result);
        assertEquals("John.Smith", result.getUser().getUsername());
        assertEquals("generatedPassword", result.getUser().getPassword());
        verify(traineeDAO).save(any(Trainee.class));
    }

    @Test
    void createTraineeProfile_NamesNull() {
        assertThrows(IllegalArgumentException.class,
                () -> traineeService.createTraineeProfile(null, "Smith", new Date(), "123 St"));
        assertThrows(IllegalArgumentException.class,
                () -> traineeService.createTraineeProfile("John", null, new Date(), "123 St"));
    }

    @Test
    void changePassword_Success() {
        when(traineeDAO.findByUsername("test.user")).thenReturn(Optional.of(testTrainee));

        traineeService.changePassword("test.user", "oldPassword", "newPassword");

        assertEquals("newPassword", testTrainee.getUser().getPassword());
        verify(traineeDAO).update(testTrainee);
    }

    @Test
    void changePassword_PasswordIncorrect() {
        when(traineeDAO.findByUsername("test.user")).thenReturn(Optional.of(testTrainee));

        assertThrows(SecurityException.class,
                () -> traineeService.changePassword("test.user", "wrongPassword", "newPassword"));
    }

    @Test
    void updateTraineeProfile_Success() {
        when(traineeDAO.findByUsername("test.user")).thenReturn(Optional.of(testTrainee));
        Date newDob = new Date();
        String newAddress = "456 New St";

        Trainee result = traineeService.updateTraineeProfile(validCredentials, newDob, newAddress);

        assertEquals(newDob, result.getDateOfBirth());
        assertEquals(newAddress, result.getAddress());
        verify(traineeDAO).update(testTrainee);
    }

    @Test
    void updateStatus_Success() {
        when(traineeDAO.findByUsername("test.user")).thenReturn(Optional.of(testTrainee));

        traineeService.updateStatus(validCredentials);
        assertFalse(testTrainee.getUser().getIsActive());

        traineeService.updateStatus(validCredentials);
        assertTrue(testTrainee.getUser().getIsActive());
    }

    @Test
    void deleteTraineeProfile_Success() {
        when(traineeDAO.findByUsername("test.user")).thenReturn(Optional.of(testTrainee));

        traineeService.deleteTraineeProfile(validCredentials);

        verify(traineeDAO).delete(1L);
    }

    @Test
    void updateTraineeTrainers_Success() {
        Trainer trainer1 = new Trainer();
        trainer1.setId(1L);
        Trainer trainer2 = new Trainer();
        trainer2.setId(2L);

        when(traineeDAO.findByUsername("test.user")).thenReturn(Optional.of(testTrainee));
        when(trainerDAO.findById(1L)).thenReturn(Optional.of(trainer1));
        when(trainerDAO.findById(2L)).thenReturn(Optional.of(trainer2));

        traineeService.updateTraineeTrainers(validCredentials, Set.of(1L, 2L));

        assertEquals(2, testTrainee.getTrainers().size());
        verify(traineeDAO).update(testTrainee);
    }

    @Test
    void updateTraineeTrainers_TrainerNotFound() {
        when(traineeDAO.findByUsername("test.user")).thenReturn(Optional.of(testTrainee));
        when(trainerDAO.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.updateTraineeTrainers(validCredentials, Set.of(1L)));
    }

    @Test
    void getTraineeProfileById_Success() {
        when(traineeDAO.findById(1L)).thenReturn(Optional.of(testTrainee));

        Optional<Trainee> result = traineeService.getById(1L);
        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }
}
