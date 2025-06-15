package com.company.gym.util;

import com.company.gym.dao.BaseAndUpdateDAO;
import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.impl.TraineeDAOImpl;
import com.company.gym.dao.impl.TrainerDAOImpl;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.service.UserService;
import com.company.gym.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsernameGeneratorTest {

    private UsernameGenerator usernameGenerator;
    private InMemoryUsernameAvailabilityChecker availabilityChecker;
    private Set<String> existingUsernames;

    @BeforeEach
    void setUp() {
        existingUsernames = new HashSet<>();
        availabilityChecker = new InMemoryUsernameAvailabilityChecker(existingUsernames);
        usernameGenerator = new UsernameGenerator(availabilityChecker);
    }

    @Test
    void generateUsername() {
        String username = usernameGenerator.generateUsername("John", "Smith");
        assertEquals("John.Smith", username);
    }

    @Test
    void generateUsername_WithDuplicate() {
        existingUsernames.add("John.Smith");

        String username = usernameGenerator.generateUsername("John", "Smith");
        assertEquals("John.Smith1", username);
    }

    @Test
    void generateUsername_WithMultipleDuplicates() {
        existingUsernames.add("John.Smith");
        existingUsernames.add("John.Smith1");
        existingUsernames.add("John.Smith2");

        String username = usernameGenerator.generateUsername("John", "Smith");
        assertEquals("John.Smith3", username);
    }

    @Test
    void generateUsername_WithEmptyNames() {
        String username = usernameGenerator.generateUsername("", "");
        assertEquals(".", username);
    }

    private static class InMemoryUsernameAvailabilityChecker implements UsernameAvailabilityChecker {

        private final Set<String> existingUsernames;

        public InMemoryUsernameAvailabilityChecker(Set<String> existingUsernames) {
            this.existingUsernames = existingUsernames;
        }

        @Override
        public boolean isUsernameTaken(String username) {
            return existingUsernames.contains(username);
        }
    }
}
