package com.company.gym.integration;

import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.dao.TrainingDAO;
import com.company.gym.dao.TrainingTypeDAO;
import com.company.gym.entity.*;
import com.company.gym.security.JwtUtil;
import com.company.trainerworkload.dao.TrainerSummaryRepository;
import com.company.trainerworkload.entity.MonthSummary;
import com.company.trainerworkload.entity.TrainerSummary;
import com.company.trainerworkload.entity.YearSummary;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@CucumberContextConfiguration
public class TrainingIntegrationSteps extends IntegrationTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired private TraineeDAO traineeDAO;
    @Autowired private TrainerDAO trainerDAO;
    @Autowired private TrainingDAO trainingDAO;
    @Autowired private TrainingTypeDAO trainingTypeDAO;
    @Autowired private TrainerSummaryRepository trainerSummaryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Trainee testTrainee;
    private Trainer testTrainer;
    private Training testTraining;

    @Before
    public void setUp() {
        trainerSummaryRepository.deleteAll();

        jdbcTemplate.execute("DELETE FROM trainings");
        jdbcTemplate.execute("DELETE FROM trainees");
        jdbcTemplate.execute("DELETE FROM trainers");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Given("a trainee with username {string} exists")
    public void createTrainee(String traineeUsername) {
        this.testTrainee = traineeDAO.findByUsername(traineeUsername).orElseGet(() -> {
            User traineeUser = new User();
            traineeUser.setFirstName("John");
            traineeUser.setLastName("Doe");
            traineeUser.setUsername(traineeUsername);
            traineeUser.setPassword("password");
            traineeUser.setIsActive(true);
            Trainee newTrainee = new Trainee();
            newTrainee.setUser(traineeUser);
            traineeDAO.save(newTrainee);
            return newTrainee;
        });
    }

    @Given("a trainer with username {string} exists")
    public void createTrainer(String trainerUsername) {
        this.testTrainer = trainerDAO.findByUsername(trainerUsername).orElseGet(() -> {
            TrainingType cardioType = trainingTypeDAO.findByName("Cardio").get();
            User trainerUser = new User();
            trainerUser.setFirstName("Jane");
            trainerUser.setLastName("Doe");
            trainerUser.setUsername(trainerUsername);
            trainerUser.setPassword("password");
            trainerUser.setIsActive(true);
            Trainer newTrainer = new Trainer();
            newTrainer.setUser(trainerUser);
            newTrainer.setSpecialization(cardioType);
            trainerDAO.save(newTrainer);
            return newTrainer;
        });
    }

    @Given("a training session exists for trainee {string} with trainer {string} with a duration of {int} minutes in month {int} of year {int}")
    public void createTraining(String traineeUsername, String trainerUsername, int duration, int month, int year) {

        TrainingType cardioType = trainingTypeDAO.findByName("Cardio").get();
        LocalDate trainingDate = LocalDate.of(year, month, 15);

        testTraining = new Training();
        testTraining.setTrainee(testTrainee);
        testTraining.setTrainer(testTrainer);
        testTraining.setTrainingType(cardioType);
        testTraining.setTrainingName("Initial Session");
        testTraining.setTrainingDate(trainingDate);
        testTraining.setDuration(duration);
        trainingDAO.save(testTraining);

        TrainerSummary summary = new TrainerSummary();
        summary.setTrainerUsername(trainerUsername);
        summary.setTrainerFirstName(testTrainer.getUser().getFirstName());
        summary.setTrainerLastName(testTrainer.getUser().getLastName());
        MonthSummary monthSummary = new MonthSummary();
        monthSummary.setTrainingSummaryDuration(duration);
        YearSummary yearSummary = new YearSummary();
        yearSummary.getMonths().put(month, monthSummary);
        summary.getYears().put(year, yearSummary);
        trainerSummaryRepository.save(summary);
    }

    @When("a POST request is made to {string} to create a new session for {string} with trainer {string} on date {string} for {int} minutes")
    public void postRequestToCreateSession(String endpoint, String traineeUsername, String trainerUsername, String trainingDate, Integer minutes) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(traineeUsername);
        String token = jwtUtil.generateToken(userDetails);
        String url = "http://localhost:" + port + endpoint;
        String requestBody = """
            {
                "trainerUsername": "%s",
                "trainingName": "End-to-End Cardio",
                "trainingTypeName": "Cardio",
                "trainingDate": "%s",
                "trainingDuration": %d
            }
            """.formatted(trainerUsername, trainingDate, minutes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @When("a DELETE request is made to {string} to delete the trainee {string}")
    public void deleteRequestToDeleteTrainee(String endpoint, String traineeUsername) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(traineeUsername);
        String token = jwtUtil.generateToken(userDetails);
        String url = "http://localhost:" + port + endpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Then("a training record should exist in the PostgreSQL database")
    public void trainingExists() {
        assertThat(trainingDAO.findAll()).hasSize(1);
    }

    @Then("{int} training records should exist in the PostgreSQL database")
    public void trainingRecordsExist(int count) {
        assertThat(trainingDAO.findAll()).hasSize(count);
    }

    @Then("the trainee {string} should no longer exist in the PostgreSQL database")
    public void trainingShouldNotExist(String traineeUsername) {
        assertThat(traineeDAO.findByUsername(traineeUsername)).isEmpty();
    }

    @Then("the training record should also be deleted from PostgreSQL")
    public void trainingShouldBeDeleted() {
        assertThat(trainingDAO.findById(testTraining.getId())).isEmpty();
    }

    @Then("the trainer workload summary for {string} in MongoDB for month {int} of year {int} should have a total duration of {int} minutes")
    public void checkTrainerWorkloadSummary(String trainerUsername, int month, int year, int expectedDuration) {
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Optional<TrainerSummary> summaryOpt = trainerSummaryRepository.findByTrainerUsername(trainerUsername);

            if (expectedDuration == 0) {
                if (summaryOpt.isPresent()) {
                    int totalDuration = summaryOpt.get().getYears().get(year).getMonths().get(month).getTrainingSummaryDuration();
                    assertThat(totalDuration).isZero();
                }
            } else {
                assertThat(summaryOpt).isPresent();
                TrainerSummary summary = summaryOpt.get();
                int totalDuration = summary.getYears().get(year).getMonths().get(month).getTrainingSummaryDuration();
                assertThat(totalDuration).isEqualTo(expectedDuration);
            }
        });
    }
}
