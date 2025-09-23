Feature: Trainer Workload Management

  Scenario: A new trainer adds their first training session
    Given the database is empty
    When a workload message is sent for trainer "john.doe" with first name "John", last name "Doe", action "ADD", date "2025-09-20", and duration 60
    Then a trainer summary for "john.doe" should exist in the database
    And the training duration for "john.doe" for month 9 of year 2025 should be 60

  Scenario: An existing trainer adds a new training session in the same month
    Given a trainer "jane.doe" exists with 120 minutes of training in month 9 of year 2025
    When a workload message is sent for trainer "jane.doe" with first name "Jane", last name "Doe", action "ADD", date "2025-09-25", and duration 45
    Then the trainer summary for "jane.doe" should be updated
    And the training duration for "jane.doe" for month 9 of year 2025 should be 165

  Scenario: An existing trainer adds a new training session in a different month
    Given a trainer "jane.doe" exists with 120 minutes of training in month 9 of year 2025
    When a workload message is sent for trainer "jane.doe" with first name "Jane", last name "Doe", action "ADD", date "2025-10-10", and duration 90
    Then the trainer summary for "jane.doe" should be updated
    And the training duration for "jane.doe" for month 10 of year 2025 should be 90
    And the training duration for "jane.doe" for month 9 of year 2025 should still be 120

  Scenario: A trainer deletes a training session
    Given a trainer "jane.doe" exists with 120 minutes of training in month 9 of year 2025
    When a workload message is sent for trainer "jane.doe" with first name "Jane", last name "Doe", action "DELETE", date "2025-09-15", and duration 30
    Then the trainer summary for "jane.doe" should be updated
    And the training duration for "jane.doe" for month 9 of year 2025 should be 90

  Scenario: Processing fails for a message with invalid data
    Given the database is empty
    When a workload message with a blank username is sent
    Then the message processing should fail
    And the database should remain empty
