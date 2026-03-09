## ADDED Requirements

### Requirement: Existing tests run with both ETL and Zeta engines
All existing jsl2psm and jsl2ui test suites SHALL be parameterized using `@ParameterizedTest` with `@EnumSource(TransformationMode.class)` to run each test case against both the ETL and Zeta transformation engines.

#### Scenario: jsl2psm test runs with ETL engine
- **WHEN** an existing jsl2psm test executes with `TransformationMode.ETL`
- **THEN** the test SHALL use the Epsilon ETL engine and produce the same results as before parameterization

#### Scenario: jsl2psm test runs with Zeta engine
- **WHEN** an existing jsl2psm test executes with `TransformationMode.ZETA`
- **THEN** the test SHALL use the Zeta Java engine and pass with equivalent assertions

#### Scenario: jsl2ui test runs with both engines
- **WHEN** an existing jsl2ui test executes with either TransformationMode
- **THEN** the test SHALL pass with the selected engine

### Requirement: DualTransformationTest compares ETL and Zeta output
The system SHALL provide `Jsl2PsmDualTransformationTest` and `Jsl2UiDualTransformationTest` classes that run the same JSL model through both ETL and Zeta engines and assert output equivalence using `ModelComparator`.

#### Scenario: jsl2psm dual test detects equivalent models
- **WHEN** a representative JSL model is transformed by both ETL and Zeta jsl2psm engines
- **THEN** `ModelComparator.assertEquivalent()` in STRUCTURAL mode SHALL pass with zero differences

#### Scenario: jsl2ui dual test detects equivalent models
- **WHEN** a representative JSL model is transformed by both ETL and Zeta jsl2ui engines
- **THEN** `ModelComparator.assertEquivalent()` in STRUCTURAL mode SHALL pass with zero differences

#### Scenario: Dual test reports differences during development
- **WHEN** `ModelComparator` detects differences between ETL and Zeta output
- **THEN** the test SHALL log a detailed report including difference type, element path, and count

### Requirement: ModelComparator from tatami-test-utils is used
The system SHALL use the `ModelComparator` class from `judo-tatami-test-utils` (test-scoped dependency) for model comparison. It SHALL NOT create a new comparator implementation.

#### Scenario: ModelComparator dependency is test-scoped
- **WHEN** the project builds
- **THEN** `judo-tatami-test-utils` SHALL be present only in test classpath

### Requirement: Phase-scoped Zeta unit tests follow TDD
During phased implementation, each phase SHALL include dedicated Zeta unit tests that test only the rules implemented in that phase. These tests SHALL be written before the implementation (TDD).

#### Scenario: Zeta unit test for namespace rules
- **WHEN** Phase 1 namespace rules are being implemented
- **THEN** a dedicated test SHALL verify Model and Package creation from a minimal JSL model using only the Zeta engine

#### Scenario: Zeta unit test is written before implementation
- **WHEN** a new set of Zeta rules is about to be implemented
- **THEN** a failing test SHALL exist before the rules are coded
