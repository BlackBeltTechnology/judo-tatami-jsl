## ADDED Requirements

### Requirement: ETL runs once per model for baseline
The test SHALL run the ETL transformation exactly once per model to produce both the baseline timing and the reference PSM model for comparison.

#### Scenario: Single ETL run
- **WHEN** a model is tested
- **THEN** ETL executes once, producing one timing measurement and one PSM model

### Requirement: ZETA runs multiple iterations
The test SHALL run the ZETA transformation N times per model (configurable via `judo.test.zeta.iterations`, default 3) and report the average timing.

#### Scenario: Default iterations
- **WHEN** a model is tested without setting `judo.test.zeta.iterations`
- **THEN** ZETA executes 3 times and the average time is reported

#### Scenario: Custom iterations
- **WHEN** `judo.test.zeta.iterations=5` is set
- **THEN** ZETA executes 5 times and the average time is reported

### Requirement: STRICT comparison by default
The test SHALL use `ModelComparator.ComparisonMode.STRICT` by default, comparing EAnnotations in addition to structural elements.

#### Scenario: Default comparison mode
- **WHEN** a model is tested without overriding `judo.test.comparison.mode`
- **THEN** STRICT comparison is used (EAnnotations are compared)

### Requirement: Warmup phase
The test SHALL support an optional warmup phase (enabled by default via `judo.test.warmup`) that runs ETL and ZETA once each before measured runs.

#### Scenario: Warmup enabled
- **WHEN** `judo.test.warmup` is true (default)
- **THEN** one warmup ETL run and one warmup ZETA run execute before measured runs

#### Scenario: Warmup disabled
- **WHEN** `judo.test.warmup=false` is set
- **THEN** no warmup runs execute

### Requirement: JSON result export
The test SHALL write results to `target/comparison-results.json` with per-model metrics and a summary section.

#### Scenario: JSON output
- **WHEN** all models have been tested
- **THEN** a JSON file is written to `target/comparison-results.json` containing model name, ETL time, ZETA average time, speedup, element counts, comparison result, and a summary

### Requirement: Console summary table
The test SHALL print a summary table to the log showing all model results with ETL time, ZETA average time, speedup, and comparison status.

#### Scenario: Summary table
- **WHEN** all models have been tested
- **THEN** a formatted table is logged with totals and average speedup

### Requirement: Bash script for easy execution
A bash script `execute-performance-tests.sh` in the project root SHALL wrap the Maven invocation with configurable flags.

#### Scenario: Default execution
- **WHEN** `./execute-performance-tests.sh` is run without flags
- **THEN** performance tests execute with default settings (3 iterations, warmup, STRICT comparison)

#### Scenario: Custom flags
- **WHEN** `./execute-performance-tests.sh --iterations 5 --no-warmup` is run
- **THEN** performance tests execute with 5 ZETA iterations and no warmup
