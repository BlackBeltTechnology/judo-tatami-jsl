## ADDED Requirements

### Requirement: JSL2PSM discovery comparison test
The system SHALL provide a `Jsl2PsmDiscoveryComparisonTest` class tagged with `@Tag("performance")` that discovers `.jsl` model files and runs both ETL and ZETA transformations, comparing output PSM models with `ModelComparator` in STRICT mode by default.

#### Scenario: Discover and test all built-in JSL models
- **WHEN** the test factory runs without any system properties
- **THEN** it SHALL discover all `.jsl` files in `src/test/resources/` (19 files), run ETL and ZETA transformations for each, compare outputs with ModelComparator STRICT mode, and print per-model timing results plus a summary

#### Scenario: External model discovery via system property
- **WHEN** `-Djudo.test.discovery.basedir=/path/to/jsl/files` is set
- **THEN** the test SHALL scan that directory for `.jsl` files and include them in addition to built-in models

#### Scenario: External model configuration via properties file
- **WHEN** `external-model-tests.properties` exists in the classpath
- **THEN** the test SHALL load model configurations from it (path, warmup, iterations parameters)

#### Scenario: Measurement excludes parsing
- **WHEN** a model is tested
- **THEN** the JSL model SHALL be parsed once before timing, and only the `transform()` call SHALL be measured for both ETL and ZETA

#### Scenario: Model comparison failure
- **WHEN** ETL and ZETA produce structurally different PSM models
- **THEN** the test SHALL fail with a detailed diff report from ModelComparator

#### Scenario: Summary test at end
- **WHEN** all models have been tested
- **THEN** a summary dynamic test SHALL print a table with model names, ETL time, ZETA time, speedup/slowdown, and comparison result

### Requirement: JSL2UI discovery comparison test
The system SHALL provide a `Jsl2UiDiscoveryComparisonTest` class tagged with `@Tag("performance")` that discovers `.jsl` model files and runs ETL-only transformations, measuring timing.

#### Scenario: ETL-only timing for jsl2ui
- **WHEN** the test factory runs
- **THEN** it SHALL discover `.jsl` files, run ETL transformation for each, and report timing (no ZETA comparison since jsl2ui has no ZETA implementation)

#### Scenario: Future ZETA support
- **WHEN** ZETA transformation is implemented for jsl2ui in the future
- **THEN** the test SHALL be structured to easily add ZETA comparison by following the same pattern as jsl2psm
