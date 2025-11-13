Feature: Trader baseline order flow

  Background:
    Given the baseline seed job has completed successfully

  Scenario: Trader one submits NSE RELIANCE buy order with sufficient margin
    When trader "trader-one" submits a "BUY" market order for instrument "RELIANCE" with quantity 10 at price 2200.00
    Then the submitted order status is "ACCEPTED"
    And the margin check recorded initial requirement "4400.00" and remaining balance "995600.00"

  Scenario: Trader one submits NSE RELIANCE buy order that breaches margin
    When trader "trader-one" submits a "BUY" market order for instrument "RELIANCE" with quantity 2000 at price 5000.00
    Then the order submission is rejected with reason containing "Insufficient margin"

