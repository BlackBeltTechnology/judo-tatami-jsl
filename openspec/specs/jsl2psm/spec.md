# jsl2psm Specification

## Purpose

Transforms JSL (JUDO Specification Language) models into PSM (Platform-Specific Model) using Eclipse Epsilon ETL scripts. This is the primary model transformation that converts the human-authored domain specification into the formal platform model.

## Architecture

The module contains two layers:

- **Java orchestration layer:** `Jsl2Psm` configures and executes the Epsilon ETL engine. `Jsl2PsmWork` wraps it as a workflow work unit. `JslExpressionToJqlExpression` translates JSL expressions into JQL query expressions.
- **Epsilon script layer:** `jslToPsm.etl` is the main entry point, supported by ~60 EOL operation files organized into `operations/jsl/data/`, `operations/jsl/type/`, `operations/jsl/namespace/`, `operations/jsl/action/`, and `transformations/psm/modules/` (behavior ETL rules).

Key relationships:
- `Jsl2Psm.Jsl2PsmParameter` (builder) → configures transformation options
- `Jsl2PsmTransformationTrace` → maps JSL source elements to PSM target elements
- `Jsl2PsmWork` → integrates with `judo-tatami-core` WorkFlow engine
- OSGi layer: `Jsl2PsmTransformationService` + `Jsl2PsmTransformationJslModelTracker` for runtime registration

## Requirements

### Requirement: JSL to PSM model transformation

The module SHALL transform a JslDslModel into a PsmModel by executing the `jslToPsm.etl` Epsilon ETL script with the JSL model as source and PSM model as target.

#### Scenario: Basic entity transformation
- **GIVEN** a JSL model containing entity declarations with fields and relations
- **WHEN** `Jsl2Psm.executeJsl2PsmTransformation()` is called with a `Jsl2PsmParameter` containing the JSL and PSM models
- **THEN** the PSM model SHALL contain corresponding entity types, attributes, and relation definitions

#### Scenario: Enum and type transformation
- **GIVEN** a JSL model containing enum declarations and primitive type definitions
- **WHEN** the transformation is executed
- **THEN** the PSM model SHALL contain equivalent enum types with literal values and data type definitions with constraints (precision, scale, min-size, max-size, regex)

### Requirement: Transfer object generation

The module SHALL generate default transfer objects for entities when `generateDefaultTransferObject` is `true` (default).

#### Scenario: Default transfer object for entity
- **GIVEN** a JSL entity declaration and `generateDefaultTransferObject=true`
- **WHEN** the transformation is executed
- **THEN** the PSM model SHALL contain a mapped transfer object type with fields corresponding to the entity's fields and relations

### Requirement: Behavior generation

The module SHALL generate CRUD and relation behaviors when `generateBehaviours` is `true`.

#### Scenario: CRUD behaviors
- **GIVEN** a JSL model with entity and transfer declarations
- **WHEN** `generateBehaviours=true` in the parameter configuration
- **THEN** the PSM model SHALL contain list, create, update, delete, and refresh behavior operations on the generated transfer objects

### Requirement: Expression translation

The module SHALL translate JSL derived expressions into JQL query expressions via `JslExpressionToJqlExpression`.

#### Scenario: Derived field expression
- **GIVEN** a JSL entity with a derived field definition (e.g., `derived String fullName => self.firstName + " " + self.lastName`)
- **WHEN** the transformation processes the derived declaration
- **THEN** the corresponding PSM attribute SHALL have a JQL expression that evaluates equivalently

### Requirement: Transformation trace generation

The module SHALL optionally produce a `Jsl2PsmTransformationTrace` mapping source JSL elements to target PSM elements when `createTrace` is `true`.

#### Scenario: Trace creation enabled
- **GIVEN** `createTrace=true` in the parameter configuration
- **WHEN** the transformation completes
- **THEN** a `Jsl2PsmTransformationTrace` SHALL be returned containing mappings from JSL model elements to their corresponding PSM model elements

### Requirement: Parallel execution support

The module SHALL support parallel Epsilon execution when `parallel` is `true` (default).

#### Scenario: Parallel execution
- **GIVEN** `parallel=true` in the parameter configuration
- **WHEN** the transformation is executed
- **THEN** the Epsilon execution context SHALL be configured for parallel rule evaluation

### Requirement: Configurable naming conventions

The module SHALL apply configurable prefix/postfix/midfix naming patterns to generated PSM elements.

#### Scenario: Custom entity name prefix
- **GIVEN** `entityNamePrefix="_"` and `entityNamePostfix=""` in the parameter configuration
- **WHEN** an entity named `Person` is transformed
- **THEN** the generated PSM entity type name SHALL use the configured prefix (e.g., `_Person`)
