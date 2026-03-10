## MODIFIED Requirements

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

#### Scenario: jsl2ui tests are parameterized like jsl2psm
- **WHEN** all 12 jsl2ui test classes are updated
- **THEN** each test method SHALL use `@ParameterizedTest @EnumSource(TransformationMode.class)` and call `transform(mode)`

### Requirement: DualTransformationTest compares ETL and Zeta output
The system SHALL provide `Jsl2PsmDualTransformationTest` and `Jsl2UiDualTransformationTest` classes that run the same JSL model through both ETL and Zeta engines and assert output equivalence using `ModelComparator`.

#### Scenario: jsl2ui dual test passes for all test models
- **WHEN** the 10 dual test cases (including the 9 currently disabled) are run
- **THEN** all SHALL pass with `ModelComparator` reporting EQUIVALENT in STRUCTURAL mode

### Requirement: Redundant zeta test classes are removed
The 5 separate `zeta/` test classes SHALL be deleted when their coverage is fully subsumed by parameterized tests running with `TransformationMode.ZETA`.

#### Scenario: Zeta test classes deleted after parameterization
- **WHEN** all 12 jsl2ui test classes pass with `TransformationMode.ZETA`
- **THEN** the 5 separate zeta/ test classes SHALL be removed from the codebase
