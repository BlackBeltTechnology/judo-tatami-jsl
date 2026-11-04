## ADDED Requirements

### Requirement: Realistic JSL model generator
The system SHALL provide a `RealisticJslModelGenerator` class in the jsl2psm test sources that generates valid JSL text strings with characteristics matching the RackInspect real-world model.

#### Scenario: Generate model with default entity count
- **WHEN** `generate(70)` is called
- **THEN** it SHALL produce a valid JSL model string with approximately 70 entities, each having 4 attributes, 2 relations, and corresponding mapped and unmapped transfer objects

#### Scenario: Generated model is parseable
- **WHEN** the generated JSL string is passed to `JslParser.getModelFromStrings()`
- **THEN** it SHALL parse successfully and produce a valid `JslDslModel`

#### Scenario: Configurable entity count
- **WHEN** `generate(N)` is called with different values of N
- **THEN** it SHALL scale entity count, transfer objects, relations, and derived properties proportionally

#### Scenario: Model includes diverse patterns
- **WHEN** a model is generated
- **THEN** it SHALL include entities with attributes (string, numeric, boolean, timestamp), relations (associations, containments), transfers (mapped and unmapped), derived fields, queries, enumerations, and actions

### Requirement: Realistic performance test
The system SHALL provide a `Jsl2PsmRealisticPerformanceTest` class tagged with `@Tag("performance")` that uses the model generator for performance measurement.

#### Scenario: Warmup then single measurement
- **WHEN** the performance test runs
- **THEN** it SHALL execute one warmup run (ETL + ZETA on fresh models), then one measured run (ETL + ZETA on fresh models), timing only the transformation (not parsing)

#### Scenario: STRICT model comparison
- **WHEN** both transformations complete
- **THEN** the outputs SHALL be compared with `ModelComparator` in STRICT mode

#### Scenario: Performance reporting
- **WHEN** the test completes
- **THEN** it SHALL log entity count, total elements, ETL time, ZETA time, throughput (elements/second), and speedup/slowdown ratio
