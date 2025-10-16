Feature: End-to-End Training Creation
  As a user, when I create a new training session in the main application,
  the system should process it and correctly update the trainer's workload summary.

  Scenario: A new training session is created and the trainer's workload is updated
    Given a trainee with username "john.doe" exists
    And a trainer with username "jane.doe" exists
    When a POST request is made to "/trainings" to create a new session for "john.doe" with trainer "jane.doe" on date "2025-09-19" for 60 minutes
    Then a training record should exist in the PostgreSQL database
    And the trainer workload summary for "jane.doe" in MongoDB for month 9 of year 2025 should have a total duration of 60 minutes

  Scenario: A second training is added for the same trainer in the same month
    Given a trainee with username "john.doe" exists
    And a trainer with username "jane.doe" exists
    And a training session exists for trainee "john.doe" with trainer "jane.doe" with a duration of 60 minutes in month 9 of year 2025
    When a POST request is made to "/trainings" to create a new session for "john.doe" with trainer "jane.doe" on date "2025-09-25" for 45 minutes
    Then 2 training records should exist in the PostgreSQL database
    And the trainer workload summary for "jane.doe" in MongoDB for month 9 of year 2025 should have a total duration of 105 minutes

  Scenario: A trainee profile is deleted, cascading to trainings and updating workload
    Given a trainee with username "john.doe" exists
    And a trainer with username "jane.doe" exists
    And a training session exists for trainee "john.doe" with trainer "jane.doe" with a duration of 60 minutes in month 9 of year 2025
    When a DELETE request is made to "/trainees/profile" to delete the trainee "john.doe"
    Then the trainee "john.doe" should no longer exist in the PostgreSQL database
    And the training record should also be deleted from PostgreSQL
    And the trainer workload summary for "jane.doe" in MongoDB for month 9 of year 2025 should have a total duration of 0 minutes
