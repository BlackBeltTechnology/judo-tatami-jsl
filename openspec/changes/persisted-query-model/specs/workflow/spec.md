# Spec Delta

## ADDED Requirements

### Requirement: Query model stage

The JSL workflow SHALL execute an `asm2query` stage that builds the query model from the run's ASM, measure and
expression models, after the parallel ASM transformation block, and puts the `QueryModel` into the
`TransformationContext`.

#### Scenario: Default pipeline produces the query model
- **GIVEN** a `DefaultWorkflowSetupParameters` with `ignoreAsm2Query` left at its default
- **WHEN** the workflow runs over a JSL model
- **THEN** the `TransformationContext` SHALL contain a `QueryModel` built from the `ExpressionModel` of the same run

#### Scenario: Stage runs after the ASM block
- **GIVEN** `runInParallel=true`
- **WHEN** the workflow is constructed
- **THEN** `asm2query` SHALL NOT run concurrently with `asm2expression`, `asm2rdbms` or `asm2keycloak`

#### Scenario: No measure model
- **GIVEN** a run that produced no measure model
- **WHEN** the `asm2query` stage executes
- **THEN** the query model SHALL be built with an empty measure set

### Requirement: Query model stage skip rules

The `asm2query` stage SHALL be skipped when `ignoreAsm2Query` is `true`, when `ignorePsm2Asm` is `true`, or when a
`QueryModel` is already in the context. When no `ExpressionModel` is in the context it SHALL be skipped with a
WARN naming the missing model, and the run SHALL succeed.

#### Scenario: Flag set
- **GIVEN** `ignoreAsm2Query=true`
- **WHEN** the workflow executes
- **THEN** no `QueryModel` SHALL be produced

#### Scenario: Expression stage disabled
- **GIVEN** `ignoreAsm2Expression=true` and `ignoreAsm2Query=false`
- **WHEN** the workflow executes
- **THEN** the stage SHALL be skipped, a WARN SHALL say the expression model is missing, and the run SHALL succeed

#### Scenario: Existing query model in context
- **GIVEN** a `QueryModel` already present in the `TransformationContext`
- **WHEN** the workflow executes
- **THEN** the stage SHALL NOT run again

### Requirement: Query model flag default

`DefaultWorkflowSetupParameters` SHALL carry `ignoreAsm2Query` with default `false`, so the query model is produced
unless a caller opts out.

#### Scenario: Default value
- **GIVEN** a `DefaultWorkflowSetupParameters` built without setting `ignoreAsm2Query`
- **WHEN** the value is read
- **THEN** it SHALL be `false`

### Requirement: Query model file save order and stale delete

`DefaultWorkflowSave` SHALL write `<name>-query.model` after the ASM, measure and expression models of the run,
only when a `QueryModel` is in the context and the expression model was saved. A save that writes the ASM model
without a `QueryModel` SHALL delete an existing query file; a save that does not write the ASM model SHALL leave it
untouched.

#### Scenario: Query file written last
- **GIVEN** a completed run with ASM, expression and query models in the context
- **WHEN** `DefaultWorkflowSave` is invoked
- **THEN** `<name>-query.model` SHALL exist in the destination, written after `<name>-asm.model` and
  `<name>-expression.model`

#### Scenario: Stale query file removed
- **GIVEN** a destination directory holding `<name>-query.model` from an earlier run and a new run with
  `ignoreAsm2Query=true`
- **WHEN** `DefaultWorkflowSave` writes the ASM model
- **THEN** no `<name>-query.model` SHALL exist after the save

#### Scenario: ASM not saved
- **GIVEN** a run that does not write the ASM model
- **WHEN** `DefaultWorkflowSave` is invoked
- **THEN** an existing `<name>-query.model` SHALL be left untouched

## MODIFIED Requirements

### Requirement: Model persistence

The workflow SHALL save all generated models to the configured destination via `DefaultWorkflowSave`, including
`<name>-query.model` when a query model was produced.

#### Scenario: Save models
- **GIVEN** a completed workflow with generated models
- **WHEN** `DefaultWorkflowSave` is invoked
- **THEN** all models (PSM, ASM, UI, RDBMS, Liquibase, Expression, Measure, Query) SHALL be persisted to the destination directory
