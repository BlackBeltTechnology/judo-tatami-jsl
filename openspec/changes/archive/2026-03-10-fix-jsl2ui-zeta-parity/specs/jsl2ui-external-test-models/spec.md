## ADDED Requirements

### Requirement: JSL2UI test models available as external files
The system SHALL provide `.jsl` model files extracted from inline test models in `src/test/resources/model/`. Each file SHALL contain a complete JSL model with entity, transfer, actor, and frontend declarations suitable for JSL2UI transformation.

#### Scenario: External model files are valid JSL
- **WHEN** a `.jsl` file from `src/test/resources/model/` is parsed by `JslParser`
- **THEN** the resulting `JslDslModel` SHALL be valid

#### Scenario: External model files are configured in properties
- **WHEN** `external-model-tests.properties` is loaded by the discovery comparison test
- **THEN** it SHALL contain entries for each extracted model file with correct paths

### Requirement: Discovery comparison test uses external UI models
The `Jsl2UiDiscoveryComparisonTest` SHALL load models from `external-model-tests.properties` and run both ETL and Zeta transformations, comparing the results using `ModelComparator`.

#### Scenario: Discovery test finds and processes external models
- **WHEN** the discovery comparison test runs
- **THEN** it SHALL produce `DynamicTest` instances for each configured model and log comparison results
