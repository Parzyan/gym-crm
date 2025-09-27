package com.company.gym.component.steps;

import com.company.gym.component.ComponentTestBase;
import com.company.gym.component.TestJmsListener;
import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.dao.TrainingDAO;
import com.company.gym.dao.TrainingTypeDAO;
import com.company.gym.dto.request.TrainerWorkloadRequest;
import com.company.gym.entity.*;
import com.company.gym.service.TrainingService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Transactional
public class TrainingStepDefinitions extends ComponentTestBase {

    @Autowired private TrainingService trainingService;
    @Autowired private TraineeDAO traineeDAO;
    @Autowired private TrainerDAO trainerDAO;
    @Autowired private TrainingTypeDAO trainingTypeDAO;
    @Autowired private TrainingDAO trainingDAO;
    @Autowired private TestJmsListener testJmsListener;

    @Before
    public void setUp() {
        testJmsListener.clear();
    }

    @Given("a training type named {string} exists")
    public void trainingTypeExists(String typeName) {
        assertThat(trainingTypeDAO.findByName(typeName)).as("Static training type '" + typeName + "' should be pre-loaded by data.sql").isPresent();
    }

    @When("a new training is created for trainee {string} and trainer {string} with type {string}, date {string}, and duration {int}")
    public void createTraining(String traineeUsername, String trainerUsername, String typeName, String date, int duration) {
        TrainingType type = trainingTypeDAO.findByName(typeName).orElseThrow(() -> new IllegalStateException("Training type not found: " + typeName));
        trainingService.createTraining(
                new Credentials(traineeUsername, null),
                new Credentials(trainerUsername, null),
                "Morning Cardio Session",
                type.getId(),
                LocalDate.parse(date),
                duration
        );
    }

    @Then("a training record for {string} and {string} should be saved in the database")
    public void trainingShouldBeSaved(String traineeUsername, String trainerUsername) {
        List<Training> trainings = trainingDAO.findAll();
        assertThat(trainings).hasSize(1);
        Training savedTraining = trainings.get(0);
        assertThat(savedTraining.getTrainee().getUser().getUsername()).isEqualTo(traineeUsername);
        assertThat(savedTraining.getTrainer().getUser().getUsername()).isEqualTo(trainerUsername);
        assertThat(savedTraining.getDuration()).isEqualTo(60);
    }

    @Then("a workload message for {string} with action {string}, date {string}, and duration {int} should be sent to the queue")
    public void workloadMessageShouldBeSent(String trainerUsername, String action, String date, int duration) {
        TrainerWorkloadRequest receivedMessage = await().atMost(5, TimeUnit.SECONDS)
                .until(() -> testJmsListener.getReceivedMessages().poll(), java.util.Objects::nonNull);
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getTrainerUsername()).isEqualTo(trainerUsername);
        assertThat(receivedMessage.getActionType().name()).isEqualTo(action);
        assertThat(receivedMessage.getTrainingDate()).isEqualTo(LocalDate.parse(date));
        assertThat(receivedMessage.getTrainingDuration()).isEqualTo(duration);
    }
}
