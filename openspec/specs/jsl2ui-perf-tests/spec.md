## ADDED Requirements

### Requirement: JSL2UI discovery performance test
The module SHALL have a `Jsl2UiDiscoveryComparisonTest` that discovers JSL models and compares ETL vs ZETA UI transformation output with configurable iterations and STRICT comparison.

#### Scenario: Default execution
- **WHEN** the test runs with default settings
- **THEN** ETL runs once per model, ZETA runs 3 times, STRICT comparison is used, and a summary table is printed

### Requirement: Bash script supports JSL2UI module
The `execute-performance-tests.sh` script SHALL accept `--module jsl2ui` to run JSL2UI performance tests.

#### Scenario: Run JSL2UI tests
- **WHEN** `./execute-performance-tests.sh --module jsl2ui` is run
- **THEN** only JSL2UI performance tests execute

#### Scenario: Run all modules
- **WHEN** `./execute-performance-tests.sh --module all` is run
- **THEN** both JSL2PSM and JSL2UI performance tests execute
