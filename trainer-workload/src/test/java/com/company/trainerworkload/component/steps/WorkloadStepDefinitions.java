package com.company.trainerworkload.component.steps;

import com.company.trainerworkload.component.ComponentTestBase;
import com.company.trainerworkload.dao.TrainerSummaryRepository;
import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.entity.ActionType;
import com.company.trainerworkload.entity.MonthSummary;
import com.company.trainerworkload.entity.TrainerSummary;
import com.company.trainerworkload.entity.YearSummary;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@CucumberContextConfiguration
public class WorkloadStepDefinitions extends ComponentTestBase {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerSummaryRepository trainerSummaryRepository;

    @Before
    public void setUp() {
        trainerSummaryRepository.deleteAll();
    }

    @Given("the database is empty")
    public void the_database_is_empty() {
        assertThat(trainerSummaryRepository.count()).isZero();
    }

    @Given("a trainer {string} exists with {int} minutes of training in month {int} of year {int}")
    public void a_trainer_exists_with_minutes_of_training_in_month_of_year(String username, int duration, int month, int year) {
        TrainerSummary summary = new TrainerSummary();
        summary.setTrainerUsername(username);
        summary.setTrainerFirstName("Jane");
        summary.setTrainerLastName("Doe");
        summary.setTrainerStatus(true);

        MonthSummary monthSummary = new MonthSummary();
        monthSummary.setTrainingSummaryDuration(duration);

        YearSummary yearSummary = new YearSummary();
        yearSummary.getMonths().put(month, monthSummary);

        summary.getYears().put(year, yearSummary);

        trainerSummaryRepository.save(summary);
    }

    @When("a workload message is sent for trainer {string} with first name {string}, last name {string}, action {string}, date {string}, and duration {int}")
    public void a_workload_message_is_sent(String username, String firstName, String lastName, String action, String date, int duration) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(username);
        request.setTrainerFirstName(firstName);
        request.setTrainerLastName(lastName);
        request.setActive(true);
        request.setActionType(ActionType.valueOf(action.toUpperCase()));
        request.setTrainingDate(LocalDate.parse(date));
        request.setTrainingDuration(duration);

        sendJmsMessage(request);
    }

    @When("a workload message with a blank username is sent")
    public void a_workload_message_with_a_blank_username_is_sent() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername("");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Doe");
        request.setActive(true);
        request.setActionType(ActionType.ADD);
        request.setTrainingDate(LocalDate.now());
        request.setTrainingDuration(60);

        sendJmsMessage(request);
    }

    @Then("a trainer summary for {string} should exist in the database")
    public void a_trainer_summary_for_should_exist_in_the_database(String username) {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Optional<TrainerSummary> summary = trainerSummaryRepository.findByTrainerUsername(username);
            assertThat(summary).isPresent();
        });
    }

    @Then("the trainer summary for {string} should be updated")
    public void the_trainer_summary_for_should_be_updated(String username) {
        a_trainer_summary_for_should_exist_in_the_database(username);
    }

    @Then("the training duration for {string} for month {int} of year {int} should be {int}")
    public void the_training_duration_for_month_of_year_should_be(String username, int month, int year, int expectedDuration) {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Optional<TrainerSummary> summaryOpt = trainerSummaryRepository.findByTrainerUsername(username);
            assertThat(summaryOpt).isPresent();

            TrainerSummary summary = summaryOpt.get();
            int actualDuration = summary.getYears()
                    .get(year)
                    .getMonths()
                    .get(month)
                    .getTrainingSummaryDuration();

            assertThat(actualDuration).isEqualTo(expectedDuration);
        });
    }

    @Then("the training duration for {string} for month {int} of year {int} should still be {int}")
    public void the_training_duration_for_month_of_year_should_still_be(String username, int month, int year, int expectedDuration) {
        the_training_duration_for_month_of_year_should_be(username, month, year, expectedDuration);
    }

    @Then("the message processing should fail")
    public void the_message_processing_should_fail() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Then("the database should remain empty")
    public void the_database_should_remain_empty() {
        assertThat(trainerSummaryRepository.count()).isZero();
    }

    private void sendJmsMessage(TrainerWorkloadRequest request) {
        jmsTemplate.convertAndSend("trainer.workload.queue", request, message -> {
            message.setStringProperty("X-Transaction-ID", "test-tx-id-cucumber");
            message.setStringProperty("_type", "com.company.gym.dto.request.TrainerWorkloadRequest");
            return message;
        });
    }
}
