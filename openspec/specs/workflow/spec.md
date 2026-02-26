# workflow Specification

## Purpose

Orchestrates the complete JUDO model transformation pipeline, wiring together all individual transformation steps (JSL→PSM, JSL→UI, PSM→ASM, PSM→Measure, ASM→RDBMS, ASM→Expression, RDBMS→Liquibase, ASM→Keycloak) into a configurable, optionally parallel workflow.

## Architecture

Key classes and their relationships:

- `AbstractTatamiPipelineWorkflow` — abstract base class that configures all transformation parameters, builds the workflow (parallel or sequential), executes it via the `WorkFlowEngine`, and collects metrics.
- `DefaultWorkflow` extends `AbstractTatamiPipelineWorkflow` — standard workflow that loads JSL models and runs the full pipeline.
- `JslDefaultWorkflow` — JSL-specific workflow variant.
- `PsmDefaultWorkflow` — PSM-specific workflow variant (starts from an existing PSM model).
- `DefaultWorkflowSetupParameters` — Lombok `@Builder` configuration object holding all transformation flags, model references, RDBMS naming options, and execution settings.
- `WorkflowHelper` — factory for creating workflow instances, loading models, and saving results.
- `DefaultWorkflowSave` — persists transformed models to the filesystem.
- `DefaultWorkflowMetricsCollector` — collects timing metrics for each transformation stage.
- `WorkflowMetrics` — metrics interface.
- `CheckWork` — validation work unit for model checking.

The workflow uses `Work` and `WorkFlow` abstractions from `judo-tatami-core` to compose transformation steps as sequential or parallel flows.

## Requirements

### Requirement: Full pipeline execution

The workflow SHALL execute the complete transformation chain: JSL→PSM→ASM→RDBMS→Liquibase, JSL→UI, PSM→Measure, ASM→Expression, and optionally ASM→Keycloak.

#### Scenario: Default pipeline
- **GIVEN** a `DefaultWorkflowSetupParameters` with all `ignore*` flags set to `false`
- **WHEN** `startDefaultWorkflow()` is called
- **THEN** all transformation steps SHALL execute and the `TransformationContext` SHALL contain PSM, ASM, UI, Measure, Expression, RDBMS, and Liquibase models

### Requirement: Selective transformation execution

The workflow SHALL skip individual transformation steps when their corresponding `ignore*` flag is `true`.

#### Scenario: Skip UI transformation
- **GIVEN** `ignoreJsl2Ui=true` in the parameters
- **WHEN** the workflow executes
- **THEN** the JSL→UI transformation SHALL be skipped and no UI model SHALL be produced

#### Scenario: Skip RDBMS and Liquibase
- **GIVEN** `ignoreAsm2Rdbms=true` and `ignoreRdbms2Liquibase=true`
- **WHEN** the workflow executes
- **THEN** no RDBMS or Liquibase models SHALL be produced

### Requirement: Parallel execution mode

The workflow SHALL run independent transformation steps in parallel when `runInParallel=true` (default).

#### Scenario: Parallel mode enabled
- **GIVEN** `runInParallel=true`
- **WHEN** the workflow is constructed
- **THEN** independent transformation steps (e.g., JSL→PSM and JSL→UI) SHALL be submitted for parallel execution via the WorkFlowEngine

#### Scenario: Sequential mode
- **GIVEN** `runInParallel=false`
- **WHEN** the workflow is constructed
- **THEN** all transformation steps SHALL execute sequentially in dependency order

### Requirement: Multi-dialect RDBMS generation

The workflow SHALL generate RDBMS and Liquibase models for each dialect in the `dialectList` parameter.

#### Scenario: Two dialects configured
- **GIVEN** `dialectList=["postgresql", "hsqldb"]`
- **WHEN** the workflow executes the ASM→RDBMS step
- **THEN** two RDBMS models and two Liquibase models SHALL be generated, one for each dialect

### Requirement: Metrics collection

The workflow SHALL collect timing metrics for each transformation step when `enableMetrics=true`.

#### Scenario: Metrics enabled
- **GIVEN** `enableMetrics=true`
- **WHEN** the workflow completes
- **THEN** `WorkflowMetrics` SHALL report the duration of each transformation step

### Requirement: Model persistence

The workflow SHALL save all generated models to the configured destination via `DefaultWorkflowSave`.

#### Scenario: Save models
- **GIVEN** a completed workflow with generated models
- **WHEN** `DefaultWorkflowSave` is invoked
- **THEN** all models (PSM, ASM, UI, RDBMS, Liquibase, Expression, Measure) SHALL be persisted to the destination directory

### Requirement: Configuration via builder pattern

The workflow SHALL be configured exclusively through `DefaultWorkflowSetupParameters.defaultWorkflowSetupParameters()` builder.

#### Scenario: Builder configuration
- **GIVEN** a `DefaultWorkflowSetupParametersBuilder`
- **WHEN** the builder is populated with model references, dialect list, naming options, and flags
- **THEN** calling `.build()` SHALL produce a valid `DefaultWorkflowSetupParameters` instance accepted by `AbstractTatamiPipelineWorkflow`
