package com.company.gym.component.steps;

import com.company.gym.component.ComponentTestBase;
import com.company.gym.component.TestJmsListener;
import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.dao.TrainingDAO;
import com.company.gym.dao.TrainingTypeDAO;
import com.company.gym.dto.request.TrainerWorkloadRequest;
import com.company.gym.entity.*;
import com.company.gym.service.TraineeService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Transactional
public class TraineeStepDefinitions extends ComponentTestBase {

    @Autowired private TraineeService traineeService;
    @Autowired private TraineeDAO traineeDAO;
    @Autowired private TrainerDAO trainerDAO;
    @Autowired private TrainingTypeDAO trainingTypeDAO;
    @Autowired private TrainingDAO trainingDAO;
    @Autowired private TestJmsListener testJmsListener;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Trainee testTrainee;
    private Trainer testTrainer;
    private Training testTraining;

    @Before
    public void setUp() {
        testJmsListener.clear();
    }

    @Given("the database is empty for trainees")
    public void databaseIsEmpty() {
        assertThat(traineeDAO.findAll()).isEmpty();
    }

    @Given("a trainee with username {string} exists in the database")
    public void createTrainee(String username) {
        this.testTrainee = traineeDAO.findByUsername(username).orElseGet(() -> {
            User traineeUser = new User();
            traineeUser.setFirstName("John");
            traineeUser.setLastName("Doe");
            traineeUser.setUsername(username);
            traineeUser.setPassword(passwordEncoder.encode(username));
            traineeUser.setIsActive(true);
            Trainee newTrainee = new Trainee();
            newTrainee.setUser(traineeUser);
            traineeDAO.save(newTrainee);
            return newTrainee;
        });
    }

    @Given("a trainer with username {string} exists in the database")
    public void createTrainer(String username) {
        this.testTrainer = trainerDAO.findByUsername(username).orElseGet(() -> {
            TrainingType cardioType = trainingTypeDAO.findByName("Cardio").get();
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

    @When("a training for trainee {string} and trainer {string} is created")
    public void createTraining(String traineeUsername, String trainerUsername) {
        TrainingType cardioType = trainingTypeDAO.findByName("Cardio").orElseThrow();
        testTraining = new Training();
        testTraining.setTrainee(testTrainee);
        testTraining.setTrainer(testTrainer);
        testTraining.setTrainingType(cardioType);
        testTraining.setTrainingName("Morning Run");
        testTraining.setTrainingDate(LocalDate.of(2025, 10, 5));
        testTraining.setDuration(60);
        trainingDAO.save(testTraining);
    }

    @When("a new trainee profile is created with first name {string} and last name {string}")
    public void newTraineeProfileIsCreated(String firstName, String lastName) {
        traineeService.createTraineeProfile(firstName, lastName, new Date(), "1 Main Street");
    }

    @When("the trainee {string} updates their address to {string}")
    public void traineeUpdatesAddress(String username, String newAddress) {
        traineeService.updateTraineeProfile(new Credentials(username, null), null, newAddress);
    }

    @When("the trainee with username {string} is deleted")
    public void traineeIsDeleted(String traineeUsername) {
        traineeService.deleteTraineeProfile(new Credentials(traineeUsername, null));
    }

    @Then("the trainee {string} should no longer exist in the database")
    public void traineeShouldNotExistInDb(String traineeUsername) {
        assertThat(traineeDAO.findByUsername(traineeUsername)).isEmpty();
    }

    @Then("the training record should also be deleted")
    public void trainingRecordShouldBeDeleted() {
        assertThat(trainingDAO.findById(testTraining.getId())).isEmpty();
    }

    @Then("a trainee user with username starting with {string} should exist in the database with an active status")
    public void traineeUserShouldExist(String usernamePrefix) {
        List<Trainee> trainees = traineeDAO.findAll();
        assertThat(trainees).hasSize(1);
        Trainee savedTrainee = trainees.get(0);
        assertThat(savedTrainee.getUser().getUsername()).startsWith(usernamePrefix);
        assertThat(savedTrainee.getUser().getIsActive()).isTrue();
    }

    @Then("the profile for {string} should be updated in the database with the {string} address")
    public void profileShouldBeUpdatedWithNewAddress(String username, String expectedAddress) {
        Trainee updatedTrainee = traineeDAO.findByUsername(username).orElseThrow();
        assertThat(updatedTrainee.getAddress()).isEqualTo(expectedAddress);
    }

    @Then("a workload message for trainer {string} with action {string} and duration {int} should be sent to the queue")
    public void workloadMessageShouldBeSent(String trainerUsername, String action, int duration) {
        TrainerWorkloadRequest receivedMessage = await().atMost(5, TimeUnit.SECONDS)
                .until(() -> testJmsListener.getReceivedMessages().poll(), java.util.Objects::nonNull);

        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getTrainerUsername()).isEqualTo(trainerUsername);
        assertThat(receivedMessage.getActionType().name()).isEqualTo(action);
        assertThat(receivedMessage.getTrainingDuration()).isEqualTo(duration);
        assertThat(receivedMessage.getTrainingDate()).isEqualTo(testTraining.getTrainingDate());
    }
}
