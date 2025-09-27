Feature: Training Creation and Workload Notification

  Scenario: A trainer and trainee successfully create a new training session
    Given a trainee with username "john.smith" exists in the database
    And a trainer with username "jane.doe" exists in the database
    And a training type named "Cardio" exists
    When a new training is created for trainee "john.smith" and trainer "jane.doe" with type "Cardio", date "2025-10-05", and duration 60
    Then a training record for "john.smith" and "jane.doe" should be saved in the database
    And a workload message for "jane.doe" with action "ADD", date "2025-10-05", and duration 60 should be sent to the queue
