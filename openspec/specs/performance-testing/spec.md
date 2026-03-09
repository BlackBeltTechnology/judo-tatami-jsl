## ADDED Requirements

### Requirement: Synthetic JSL model generator for performance testing
The system SHALL provide a `RealisticJslModelGenerator` class that creates a JSL model with characteristics similar to a real-world application (based on RackInspect). The generator SHALL be configurable for element count (default ~10,000 elements).

#### Scenario: Generator creates model with realistic characteristics
- **WHEN** `RealisticJslModelGenerator` is invoked with default parameters
- **THEN** the generated JSL model SHALL contain a realistic mix of entity declarations, transfer objects, relations, attributes, derived properties, actions, actors, enums, and queries

#### Scenario: Generator element count is configurable
- **WHEN** `RealisticJslModelGenerator` is invoked with a custom element count
- **THEN** the generated model SHALL scale proportionally to the requested count

### Requirement: Performance comparison test for jsl2psm
The system SHALL provide a `Jsl2PsmPerformanceTest` that runs the synthetic model through both ETL and Zeta engines, measuring and comparing execution times.

#### Scenario: Performance test reports timing comparison
- **WHEN** the jsl2psm performance test runs
- **THEN** it SHALL log ETL time, Zeta time, and speedup factor

#### Scenario: Performance test verifies output equivalence
- **WHEN** the performance test completes both transformations
- **THEN** it SHALL verify that ETL and Zeta outputs are structurally equivalent using `ModelComparator`

### Requirement: Performance comparison test for jsl2ui
The system SHALL provide a `Jsl2UiPerformanceTest` with the same structure as the jsl2psm performance test.

#### Scenario: jsl2ui performance test reports timing
- **WHEN** the jsl2ui performance test runs
- **THEN** it SHALL log ETL time, Zeta time, and speedup factor

### Requirement: Performance tests use @Tag annotation
Performance tests SHALL be annotated with `@Tag("performance")` to allow selective execution via Maven Surefire `-Dgroups=performance`.

#### Scenario: Performance tests are excluded from default build
- **WHEN** `mvn clean test` is run without groups parameter
- **THEN** performance tests SHALL NOT execute

#### Scenario: Performance tests run with groups parameter
- **WHEN** `mvn clean test -Dgroups=performance` is run
- **THEN** performance tests SHALL execute

### Requirement: Model generator does not use external models directly
The `RealisticJslModelGenerator` SHALL build models programmatically in test code. It SHALL NOT load or depend on external model files (e.g., RackInspect). The model characteristics SHALL be derived from analysis of a real model but created synthetically.

#### Scenario: Generator has no external file dependencies
- **WHEN** the performance test runs
- **THEN** it SHALL NOT read any files outside the project's test resources
