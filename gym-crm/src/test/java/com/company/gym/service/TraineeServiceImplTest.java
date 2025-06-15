package com.company.gym.service;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.TrainingDAO;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Training;
import com.company.gym.entity.User;
import com.company.gym.service.impl.AuthenticationServiceImpl;
import com.company.gym.service.impl.TraineeServiceImpl;
import com.company.gym.util.PasswordGenerator;
import com.company.gym.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TraineeServiceImplTest {

    private TraineeDAO traineeDAO;
    private TrainingDAO trainingDAO;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private AuthenticationServiceImpl authenticationService;

    private TraineeServiceImpl traineeService;

    private Trainee testTrainee;
    private User testUser;

    @BeforeEach
    void setUp() {
        traineeDAO = mock(TraineeDAO.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
        authenticationService = mock(AuthenticationServiceImpl.class);

        traineeService = new TraineeServiceImpl();
        traineeService.setTraineeDAO(traineeDAO);
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);
        traineeService.setAuthenticationService(authenticationService);

        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Smith");
        testUser.setUsername("John.Smith");
        testUser.setPassword("password12");
        testUser.setIsActive(true);

        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUser(testUser);
        testTrainee.setDateOfBirth(new Date(1990, 1, 1));
        testTrainee.setAddress("123 Main St");

        when(authenticationService.authenticateUser(any(), any())).thenReturn(true);
    }

    @Test
    void createTraineeProfile_Success() {
        when(usernameGenerator.generateUsername("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("password12");

        Trainee result = traineeService.createTraineeProfile("John", "Smith",
                new Date(1990, 1, 1), "123 Main St");

        assertNotNull(result);
        assertEquals("John.Smith", result.getUser().getUsername());
        assertEquals("password12", result.getUser().getPassword());
        assertTrue(result.getUser().getIsActive());
        verify(traineeDAO, times(1)).save(any(Trainee.class));
    }

    @Test
    void getTraineeByUsername_Success() {
        when(traineeDAO.findByUsername("John.Smith")).thenReturn(Optional.of(testTrainee));
        Optional<Trainee> result = traineeService.getByUsername("John.Smith");
        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    void getTraineeByUsername_NotFound() {
        when(traineeDAO.findByUsername("unknown")).thenReturn(Optional.empty());
        Optional<Trainee> result = traineeService.getByUsername("unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    void getTraineeById_Success() {
        when(traineeDAO.findById(1L)).thenReturn(Optional.of(testTrainee));
        Optional<Trainee> result = traineeService.getById(1L);
        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    void getTraineeById_NotFound() {
        when(traineeDAO.findById(100L)).thenReturn(Optional.empty());
        Optional<Trainee> result = traineeService.getById(100L);
        assertTrue(result.isEmpty());
    }

    @Test
    void changeTraineePassword_Success() {
        when(traineeDAO.findByUsername("John.Smith")).thenReturn(Optional.of(testTrainee));

        traineeService.changeTraineePassword("John.Smith", "password12", "password13");

        assertEquals("password13", testTrainee.getUser().getPassword());
        verify(traineeDAO, times(1)).update(testTrainee);
    }

    @Test
    void changeTraineePassword_Failure_WrongOldPassword() {
        when(traineeDAO.findByUsername("John.Smith")).thenReturn(Optional.of(testTrainee));

        assertThrows(SecurityException.class, () ->
                traineeService.changeTraineePassword("John.Smith", "wrong", "password13"));
    }

    @Test
    void updateTraineeProfile() {
        when(traineeDAO.findByUsername("John.Smith")).thenReturn(Optional.of(testTrainee));

        Trainee updated = traineeService.updateTraineeProfile("password12", "John.Smith",
                new Date(1995, 5, 5), "456 New St");

        assertEquals(new Date(1995, 5, 5), updated.getDateOfBirth());
        assertEquals("456 New St", updated.getAddress());
        verify(traineeDAO, times(1)).update(testTrainee);
    }

    @Test
    void updateTraineeStatus() {
        when(traineeDAO.findByUsername("John.Smith")).thenReturn(Optional.of(testTrainee));

        traineeService.updateTraineeStatus("password12", "John.Smith");

        assertFalse(testTrainee.getUser().getIsActive());
        verify(traineeDAO, times(1)).update(testTrainee);
    }

    @Test
    void deleteTraineeProfile_Success() {
        Trainee trainee = testTrainee;
        when(authenticationService.authenticateUser("John.Smith", "password12")).thenReturn(true);
        when(traineeDAO.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        doNothing().when(traineeDAO).delete(1L);

        traineeService.deleteTraineeProfile("password12", "John.Smith");

        verify(traineeDAO).delete(1L);
        verify(authenticationService).authenticateUser("John.Smith", "password12");
    }

    @Test
    void deleteTraineeProfile_WithTrainings_CascadesDeletion() {
        Trainee trainee = testTrainee;
        Training training1 = new Training();
        Training training2 = new Training();
        trainee.setTrainings(new ArrayList<>(Arrays.asList(training1, training2)));

        when(traineeDAO.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        doAnswer(invocation -> {
            trainee.getTrainings().clear();
            return null;
        }).when(traineeDAO).delete(1L);

        traineeService.deleteTraineeProfile("password12", "John.Smith");

        verify(traineeDAO).delete(1L);
        assertTrue(trainee.getTrainings().isEmpty(), "Trainings should be cleared");
    }

    @Test
    void createTraineeProfile_MissingFirstName() {
        assertThrows(IllegalArgumentException.class, () ->
                traineeService.createTraineeProfile(null, "Doe", new Date(), "Address"));
    }
}
