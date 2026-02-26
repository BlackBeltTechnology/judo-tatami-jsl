# workflow-maven-plugin Specification

## Purpose

Provides a Maven plugin (Mojo) that integrates the JUDO model transformation pipeline into the Maven build lifecycle. Exposes the `default-model-workflow` goal that reads `.jsl` source files and executes the full transformation pipeline during the compile phase.

## Architecture

Key classes:

- `DefaultWorkflowMojo` — the main Maven Mojo, annotated with `@Mojo(name = "default-model-workflow", defaultPhase = LifecyclePhase.COMPILE)`. Maps Maven `<configuration>` parameters to `DefaultWorkflowSetupParameters` and executes the workflow.
- `AbstractJslDslWorkflowProjectMojo` — abstract base class handling JSL source loading, model parsing, and common Mojo infrastructure.
- `ArtifactResolver` — resolves Maven dependency artifacts for use as JSL sources.
- `DialectParam` — configuration object for database dialect specification.
- `ResourceList` — utility for scanning resources from directories and archives.

## Requirements

### Requirement: Maven lifecycle integration

The plugin SHALL execute during the Maven COMPILE phase by default and be invocable via the `default-model-workflow` goal.

#### Scenario: Default phase execution
- **GIVEN** a Maven project with the plugin configured and no explicit `<phase>` override
- **WHEN** `mvn compile` is executed
- **THEN** the `default-model-workflow` goal SHALL execute and produce transformed models in the destination directory

### Requirement: JSL source discovery

The plugin SHALL discover `.jsl` source files from the configured `sources` parameter, scanning directories recursively.

#### Scenario: Directory source
- **GIVEN** `<sources>${project.basedir}/src/main/model</sources>` containing `.jsl` files
- **WHEN** the plugin executes
- **THEN** all `.jsl` files in the directory (recursively) SHALL be parsed and transformed

#### Scenario: Maven dependency sources
- **GIVEN** `<useDependencies>true</useDependencies>` and project dependencies containing `.jsl` files
- **WHEN** the plugin executes
- **THEN** `.jsl` files from transitive dependencies SHALL be discovered and included via `ArtifactResolver`

### Requirement: Parameter mapping to workflow

The plugin SHALL map all Maven `<configuration>` parameters to `DefaultWorkflowSetupParameters` fields.

#### Scenario: RDBMS configuration passthrough
- **GIVEN** `<rdbmsTablePrefix>TBL_</rdbmsTablePrefix>` and `<dialects>postgresql</dialects>` in plugin configuration
- **WHEN** the plugin creates the workflow
- **THEN** `DefaultWorkflowSetupParameters.rdbmsTablePrefix` SHALL be `"TBL_"` and `dialectList` SHALL contain `["postgresql"]`

### Requirement: Model output persistence

The plugin SHALL save all generated models to the `destination` directory (default: `${project.basedir}/target/model`).

#### Scenario: Default destination
- **GIVEN** no explicit `<destination>` configuration
- **WHEN** the plugin completes execution
- **THEN** generated models SHALL be written to `${project.basedir}/target/model`

### Requirement: Pre-loaded model support

The plugin SHALL accept optional pre-loaded models (PSM, ASM, Measure, Expression) to skip upstream transformation steps.

#### Scenario: Pre-loaded PSM model
- **GIVEN** `<psm>path/to/existing/psm.model</psm>` in plugin configuration
- **WHEN** the plugin executes
- **THEN** the JSL→PSM step SHALL be skipped and the pre-loaded PSM model SHALL be used for downstream transformations

### Requirement: Selective model name filtering

The plugin SHALL support filtering which models to process via the `modelNames` parameter when multiple `.jsl` files exist.

#### Scenario: Named model selection
- **GIVEN** a source directory containing `SalesModel.jsl` and `HRModel.jsl`, and `<modelNames>SalesModel</modelNames>`
- **WHEN** the plugin executes
- **THEN** only `SalesModel` SHALL be processed
