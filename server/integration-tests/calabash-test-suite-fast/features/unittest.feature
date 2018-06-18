Feature: Running the unit test
  Scenario: Running the unit test
    Then I start the app
    Then I wait for the unit test result
    Then I print it to stdout
    Then I fail if the unit test did not succeed
