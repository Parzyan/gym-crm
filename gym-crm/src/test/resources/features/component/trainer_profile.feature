Feature: Trainer Profile Management
  As a system, I need to manage the lifecycle of a trainer,
  ensuring their profiles are created and updated correctly.

  Scenario: A new trainer profile is successfully created
    Given a training type named "Strength" exists
    When a new trainer profile is created with first name "Emily", last name "Davis", and specialization "Strength"
    Then a trainer user with username starting with "Emily.Davis" should exist

  Scenario: A trainer's status is toggled from active to inactive
    Given an active trainer with username "jane.doe" exists
    When the status for trainer "jane.doe" is updated
    Then the trainer "jane.doe" should have an inactive status in the database
