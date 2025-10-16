package com.company.gym.component.steps;

import com.company.gym.component.ComponentTestBase;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.dao.TrainingTypeDAO;
import com.company.gym.entity.Credentials;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.TrainingType;
import com.company.gym.entity.User;
import com.company.gym.service.TrainerService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class TrainerStepDefinitions extends ComponentTestBase {

    @Autowired private TrainerService trainerService;
    @Autowired private TrainerDAO trainerDAO;
    @Autowired private TrainingTypeDAO trainingTypeDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Given("an active trainer with username {string} exists")
    public void createTrainer(String username) {
        TrainingType cardioType = trainingTypeDAO.findByName("Cardio").get();

        trainerDAO.findByUsername(username).orElseGet(() -> {
            User trainerUser = new User();
            trainerUser.setFirstName("Jane");
            trainerUser.setLastName("Doe");
            trainerUser.setUsername(username);
            trainerUser.setPassword(passwordEncoder.encode("password"));
            trainerUser.setIsActive(true);
            Trainer newTrainer = new Trainer();
            newTrainer.setUser(trainerUser);
            newTrainer.setSpecialization(cardioType);
            trainerDAO.save(newTrainer);
            return newTrainer;
        });
    }

    @When("a new trainer profile is created with first name {string}, last name {string}, and specialization {string}")
    public void createTrainerProfile(String firstName, String lastName, String specialization) {
        TrainingType type = trainingTypeDAO.findByName(specialization)
                .orElseThrow(() -> new IllegalStateException("Training type not found: " + specialization));
        trainerService.createTrainerProfile(firstName, lastName, type.getId());
    }

    @When("the status for trainer {string} is updated")
    public void trainerStatusIsUpdated(String username) {
        trainerService.updateStatus(new Credentials(username, "password"));
    }

    @Then("a trainer user with username starting with {string} should exist")
    public void trainerShouldExist(String username) {
        List<Trainer> trainers = trainerDAO.findAll();
        Trainer savedTrainer = trainers.stream()
                .filter(t -> t.getUser().getUsername().startsWith(username))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No trainer found with username: " + username));

        assertThat(savedTrainer.getUser().getIsActive()).isTrue();
    }

    @Then("the trainer {string} should have an inactive status in the database")
    public void trainerShouldBeInactive(String username) {
        Trainer trainer = trainerDAO.findByUsername(username).orElseThrow();
        assertThat(trainer.getUser().getIsActive()).isFalse();
    }
}
