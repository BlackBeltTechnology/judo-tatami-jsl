## ADDED Requirements

### Requirement: Exclude performance tests from normal builds
The parent pom.xml SHALL configure the maven-surefire-plugin to exclude tests tagged with `@Tag("performance")` by default.

#### Scenario: Normal build skips performance tests
- **WHEN** `mvn test` runs without any special flags
- **THEN** tests tagged with `@Tag("performance")` SHALL NOT execute

#### Scenario: Existing tests unaffected
- **WHEN** `mvn test` runs
- **THEN** all 163 existing jsl2psm tests and all jsl2ui tests SHALL still execute normally

### Requirement: Performance Maven profile
The parent pom.xml SHALL provide a `performance` profile that overrides the default surefire configuration to run only performance tests.

#### Scenario: Run only performance tests
- **WHEN** `mvn test -Pperformance` runs
- **THEN** only tests tagged with `@Tag("performance")` SHALL execute

#### Scenario: Module-specific performance tests
- **WHEN** `mvn test -Pperformance -pl judo-tatami-jsl-jsl2psm` runs
- **THEN** only jsl2psm performance tests SHALL execute

### Requirement: Comparison system properties passthrough
The surefire configuration SHALL pass comparison-related system properties to the test JVM.

#### Scenario: Override comparison mode
- **WHEN** `mvn test -Pperformance -Djudo.test.comparison.mode=STRUCTURAL` runs
- **THEN** ModelComparator SHALL use STRUCTURAL mode instead of STRICT
