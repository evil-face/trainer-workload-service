Feature: Training workload service
  Scenario: User added new training to the main service
    Given the new message with trainer workload payload of 111 minutes
    When the message comes to this microservice
    Then the training workload is added to the database

  Scenario: Main service produced bad message with incorrect payload
    Given the new message with incorrect trainer workload payload
    When the message comes to this microservice
    Then the training workload will not be added to database

  Scenario: Trainee was deleted in the main service
    Given the trainer with existing payload of 100 minutes
    And the new message with delete payload of 50 minutes
    When the message comes to this microservice
    Then the trainer with existing payload will have 50 minutes left