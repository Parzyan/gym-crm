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
    public void the_database_is_empty_for_trainees() {
        assertThat(traineeDAO.findAll()).isEmpty();
    }

    @Given("a trainee with username {string} exists in the database")
    public void a_trainee_exists_in_the_database(String username) {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("password"));
        user.setIsActive(true);
        testTrainee = new Trainee();
        testTrainee.setUser(user);
        testTrainee.setAddress("Old Address");
        traineeDAO.save(testTrainee);
    }

    @Given("a training session exists for trainee {string} with trainer {string}")
    public void a_training_session_exists_for_trainee_with_trainer(String traineeUsername, String trainerUsername) {
        TrainingType cardioType = trainingTypeDAO.findByName("Cardio")
                .orElseThrow(() -> new IllegalStateException("Static training type 'Cardio' not found. Check data.sql"));

        this.testTrainee = traineeDAO.findByUsername(traineeUsername).orElseGet(() -> {
            User traineeUser = new User();
            traineeUser.setFirstName("John");
            traineeUser.setLastName("Doe");
            traineeUser.setUsername(traineeUsername);
            traineeUser.setPassword(passwordEncoder.encode("password"));
            traineeUser.setIsActive(true);
            Trainee newTrainee = new Trainee();
            newTrainee.setUser(traineeUser);
            traineeDAO.save(newTrainee);
            return newTrainee;
        });

        this.testTrainer = trainerDAO.findByUsername(trainerUsername).orElseGet(() -> {
            User trainerUser = new User();
            trainerUser.setFirstName("Jane");
            trainerUser.setLastName("Doe");
            trainerUser.setUsername(trainerUsername);
            trainerUser.setPassword(passwordEncoder.encode("password"));
            trainerUser.setIsActive(true);
            Trainer newTrainer = new Trainer();
            newTrainer.setUser(trainerUser);
            newTrainer.setSpecialization(cardioType);
            trainerDAO.save(newTrainer);
            return newTrainer;
        });

        testTraining = new Training();
        testTraining.setTrainee(testTrainee);
        testTraining.setTrainer(testTrainer);
        testTraining.setTrainingType(cardioType);
        testTraining.setTrainingName("Morning Run");
        testTraining.setTrainingDate(LocalDate.of(2025, 10, 5));
        testTraining.setDuration(60);
        trainingDAO.save(testTraining);

        assertThat(trainingDAO.findById(testTraining.getId())).isPresent();
    }

    @When("a new trainee profile is created with first name {string} and last name {string}")
    public void a_new_trainee_profile_is_created(String firstName, String lastName) {
        traineeService.createTraineeProfile(firstName, lastName, new Date(), "1 Main Street");
    }

    @When("the trainee {string} updates their address to {string}")
    public void the_trainee_updates_their_address(String username, String newAddress) {
        traineeService.updateTraineeProfile(new Credentials(username, null), null, newAddress);
    }

    @When("the trainee with username {string} is deleted")
    public void the_trainee_with_username_is_deleted(String traineeUsername) {
        traineeService.deleteTraineeProfile(new Credentials(traineeUsername, null));
    }

    @Then("the trainee {string} should no longer exist in the database")
    public void the_trainee_should_no_longer_exist_in_the_database(String traineeUsername) {
        assertThat(traineeDAO.findByUsername(traineeUsername)).isEmpty();
    }

    @Then("the training record should also be deleted")
    public void the_training_record_should_also_be_deleted() {
        assertThat(trainingDAO.findById(testTraining.getId())).isEmpty();
    }

    @Then("a trainee user with username starting with {string} should exist in the database with an active status")
    public void a_trainee_user_should_exist(String usernamePrefix) {
        List<Trainee> trainees = traineeDAO.findAll();
        assertThat(trainees).hasSize(1);
        Trainee savedTrainee = trainees.get(0);
        assertThat(savedTrainee.getUser().getUsername()).startsWith(usernamePrefix);
        assertThat(savedTrainee.getUser().getIsActive()).isTrue();
    }

    @Then("the profile for {string} should be updated in the database with the {string} address")
    public void the_profile_for_should_be_updated_with_new_address(String username, String expectedAddress) {
        Trainee updatedTrainee = traineeDAO.findByUsername(username).orElseThrow();
        assertThat(updatedTrainee.getAddress()).isEqualTo(expectedAddress);
    }

    @Then("a workload message for trainer {string} with action {string} and duration {int} should be sent to the queue")
    public void a_workload_message_for_trainer_with_action_and_duration_should_be_sent(String trainerUsername, String action, int duration) {
        TrainerWorkloadRequest receivedMessage = await().atMost(5, TimeUnit.SECONDS)
                .until(() -> testJmsListener.getReceivedMessages().poll(), java.util.Objects::nonNull);

        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getTrainerUsername()).isEqualTo(trainerUsername);
        assertThat(receivedMessage.getActionType().name()).isEqualTo(action);
        assertThat(receivedMessage.getTrainingDuration()).isEqualTo(duration);
        assertThat(receivedMessage.getTrainingDate()).isEqualTo(testTraining.getTrainingDate());
    }
}
