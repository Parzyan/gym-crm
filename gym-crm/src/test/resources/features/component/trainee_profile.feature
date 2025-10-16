Feature: Trainee Profile Management

  Scenario: A new trainee profile is successfully created
    Given the database is empty for trainees
    When a new trainee profile is created with first name "Peter" and last name "Jones"
    Then a trainee user with username starting with "Peter.Jones" should exist in the database with an active status

  Scenario: An existing trainee successfully updates their profile
    Given a trainee with username "john.doe" exists in the database
    When the trainee "john.doe" updates their address to "123 New Street"
    Then the profile for "john.doe" should be updated in the database with the "123 New Street" address

  Scenario: A trainee with an active training session is deleted
    Given a trainee with username "john.doe" exists in the database
    And a trainer with username "jane.doe" exists in the database
    When a training for trainee "john.doe" and trainer "jane.doe" is created
    And the trainee with username "john.doe" is deleted
    Then the trainee "john.doe" should no longer exist in the database
    And the training record should also be deleted
    And a workload message for trainer "jane.doe" with action "DELETE" and duration 60 should be sent to the queue
